package jsf;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;
import entities.DocumentoOficial;
import java.io.IOException;
import jsf.util.JsfUtil;
import jsf.util.PaginationHelper;
import sessions.DocumentoOficialFacade;

        
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@ManagedBean(name = "documentoOficialController")
@SessionScoped
public class DocumentoOficialController implements Serializable {
   
    private DocumentoOficial current;
    private DataModel items = null;
    @EJB
    private sessions.DocumentoOficialFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    public boolean isEditing = true;
    

    public DocumentoOficialController() {
        System.out.println("Initializing managed bean");
    }

    public DocumentoOficial getSelected() {
        if (current == null) {
            current = new DocumentoOficial();
            selectedItemIndex = -1;
        }
        return current;
    }

    private DocumentoOficialFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10000) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "view_list";
    }

    public String prepareView() {
        current = (DocumentoOficial) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "view_view";
    }

    public String prepareCreate() {
        current = new DocumentoOficial();
        selectedItemIndex = -1;
        return "/documentoOficial/view_create";
    }

    public String create() {
        try {
            current.setDtDoc(this.currentSqlDate());
            if(current.getTipo() == 1)
                current.setNum(this.getCurrentDocNumber(1));
            else if(current.getTipo() == 2)
                current.setNum(this.getCurrentDocNumber(2));
            
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("DocumentoOficialCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (DocumentoOficial) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("DocumentoOficialUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (DocumentoOficial) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("DocumentoOficialDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass = DocumentoOficial.class)
    public static class DocumentoOficialControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DocumentoOficialController controller = (DocumentoOficialController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "documentoOficialController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof DocumentoOficial) {
                DocumentoOficial o = (DocumentoOficial) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + DocumentoOficial.class.getName());
            }
        }

    }
    
    public DocumentoOficial getLastItem(int type){
        return getFacade().getLastItem(type);
    }
    
    public int getLastNumber(int type)
    {
        return getFacade().getLastNumber(type);
    }
    
    public String getCurrentdate()
    {
       DocumentoOficial doc = new DocumentoOficial();
       return doc.getCurrentDate();
    }
    
    public String getCurrentDocNumberAndYear(int type)
    {
        DocumentoOficial doc = getLastItem(type);
        Date dt = new Date();
        
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateFormat.format(dt); 
        cal.setTime(dt);
        int current_year = cal.get(Calendar.YEAR);
        
        if (doc == null)
            return "1/" + current_year;

        dt = doc.getDtDoc();

        cal.setTime(dt);
        int last_doc_year = cal.get(Calendar.YEAR);
        
        if (current_year == last_doc_year)
        {
            return (getLastNumber(type) + 1) + "/" + current_year;
        }
        else
        {
            return "1/" + current_year;
        }
    }
    
    public Date currentSqlDate() throws ParseException
    {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String dateStr = month+1 + "/" + day + "/" + year;
        
        if(current != null)
        {
            current.setDtDoc(df.parse(dateStr));
        }
        else
        {
            current = new DocumentoOficial();
            current.setDtDoc(df.parse(dateStr));
        }
        
        return current.getDtDoc();
    }
    
    public int getCurrentDocNumber(int type)
    {
        int result = (getLastNumber(type) + 1);
        return result;
    }    
}
