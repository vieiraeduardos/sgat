package jsf;

import entities.Destinatario;
import jsf.util.JsfUtil;
import jsf.util.PaginationHelper;
import sessions.DestinatarioFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

@ManagedBean(name = "destinatarioController")
@SessionScoped
public class DestinatarioController implements Serializable {

    private Destinatario current;
    private DataModel items = null;
    @EJB
    private sessions.DestinatarioFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public DestinatarioController() {
    }

    public Destinatario getSelected() {
        if (current == null) {
            current = new Destinatario();
            selectedItemIndex = -1;
        }
        return current;
    }

    private DestinatarioFacade getFacade() {
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
        return "List";
    }

    public String prepareView() {
        current = (Destinatario) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Destinatario();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/BundleDestinatario").getString("DestinatarioCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/BundleDestinatario").getString("PersistenceErrorOccured"));
            return null;
        }
    }
    
    public String createDestinatarioInDocOficialPage()
    {
        try {
            if(!this.validteInsertSubmission()){
                FacesMessage msg = new FacesMessage("O destinatário "+getSelected().getNome()+" já existe no banco de dados");
                System.out.println("Deu pau");
                throw new ValidatorException(msg);
            }
            getFacade().create(current);
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/BundleDestinatario").getString("PersistenceErrorOccured"));
        }
        return "view_create";
    }
    
    private boolean validteInsertSubmission()
    {
        List<Destinatario> rs = getFacade().getAllItems();
        if(rs != null)
            for (Destinatario item : rs)
            {
                System.out.println();
                String db_recipient = item.getNome().trim();
                String new_recipient = getSelected().getNome().trim();
                if(new_recipient.equals(db_recipient))
                    return false;
            }
        return true;
    }

    public String prepareEdit() {
        current = (Destinatario) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/BundleDestinatario").getString("DestinatarioUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/BundleDestinatario").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Destinatario) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/BundleDestinatario").getString("DestinatarioDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/resources/BundleDestinatario").getString("PersistenceErrorOccured"));
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
    
    public List<Destinatario> getAllItems()
    {
        return getFacade().getAllItems();
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

    @FacesConverter(forClass = Destinatario.class)
    public static class DestinatarioControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DestinatarioController controller = (DestinatarioController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "destinatarioController");
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
            if (object instanceof Destinatario) {
                Destinatario o = (Destinatario) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Destinatario.class.getName());
            }
        }
    }
    
    public String getDestinatarioName(int id)
    {
        Destinatario dest = getFacade().getDestinatarioByID(id);
        if(dest != null)
            return dest.getNome();
        else
            return "Nulo";
    }
}
