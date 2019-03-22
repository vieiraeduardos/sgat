/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sessions;

import entities.AbstractFacade;
import entities.Destinatario;
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
public class DestinatarioFacade extends AbstractFacade<Destinatario> {
    @PersistenceContext(unitName = "SINTS_MAPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DestinatarioFacade() {
        super(Destinatario.class);
    }
    
    public Destinatario getDestinatarioByID(int id)
    {
        System.out.println("ID do destinatario: " + id);
        TypedQuery<Destinatario> query = em.createNamedQuery("Destinatario.findById", Destinatario.class);
        query.setParameter("id", id);
        Destinatario dest = query.getSingleResult();
        if(dest == null)
            return null;
        return dest;
    }
    
    public List<Destinatario> getAllItems()
    {
        TypedQuery<Destinatario> query = em.createNamedQuery("Destinatario.findAll", Destinatario.class);
        List<Destinatario> rs = query.getResultList();
        return rs;
    }
}
