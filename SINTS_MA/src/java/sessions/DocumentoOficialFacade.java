/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessions;

import entities.DocumentoOficial;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author HUUFMA
 */
@Stateless
public class DocumentoOficialFacade extends AbstractFacade<DocumentoOficial> {
    @PersistenceContext(unitName = "SINTS_MAPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DocumentoOficialFacade() {
        super(DocumentoOficial.class);
    }
    
    public DocumentoOficial getLastItem(int type){
        TypedQuery<DocumentoOficial> query = em.createNamedQuery("DocumentoOficial.findByTipo", DocumentoOficial.class);
        query.setParameter("tipo", type);
        List<DocumentoOficial> list = query.getResultList();
        
        if(list.size() != 0)        
            return list.get(list.size() - 1);
        return null;    
    }
    
    public int getLastNumber(int type)
    {
        TypedQuery<DocumentoOficial> query = em.createNamedQuery("DocumentoOficial.findByTipo", DocumentoOficial.class);
        query.setParameter("tipo", type);
        List<DocumentoOficial> list = query.getResultList();
        
        return list.size();
    }
}
