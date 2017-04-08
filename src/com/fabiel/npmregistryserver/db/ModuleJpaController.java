/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.npmregistryserver.db;

import com.fabiel.npmregistryserver.db.exceptions.NonexistentEntityException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author user
 */
public class ModuleJpaController implements Serializable {

    public ModuleJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Module module) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(module);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Module module) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            module = em.merge(module);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = module.getId();
                if (findModule(id) == null) {
                    throw new NonexistentEntityException("The module with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Module module;
            try {
                module = em.getReference(Module.class, id);
                module.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The module with id " + id + " no longer exists.", enfe);
            }
            em.remove(module);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Module> findModuleEntities() {
        return findModuleEntities(true, -1, -1);
    }

    public List<Module> findModuleEntities(int maxResults, int firstResult) {
        return findModuleEntities(false, maxResults, firstResult);
    }

    private List<Module> findModuleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
//            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
//            cq.select(cq.from(Module.class));
            Query q = em.createQuery("SELECT e FROM Module e");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Module> filterModuleEntities(String filter, boolean all, int maxResults, int firstResult) {
        filter = filter.toLowerCase();
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("SELECT DISTINC e FROM Module e WHERE LOWER(e.id) LIKE :filterString OR LOWER(e.description) LIKE :filterString OR :filter MEMBER OF e.keywords");
//            Query q = em.createQuery("SELECT e,k FROM Module e JOIN e.keywords k WHERE LOWER(e.id) LIKE '%:filter%' OR LOWER(e.description)  LIKE '%:filter%' OR :filter MEMBER OF e.keywords");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            q.setParameter("filter", filter);
            q.setParameter("filterString", "%"+filter+"%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Module findModule(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Module.class, id);
        } finally {
            em.close();
        }
    }

    public int getModuleCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("SELECT COUNT(e) FROM Module e");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
