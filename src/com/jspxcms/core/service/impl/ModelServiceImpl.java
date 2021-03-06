package com.jspxcms.core.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jspxcms.common.orm.Limitable;
import com.jspxcms.common.orm.RowSide;
import com.jspxcms.common.orm.SearchFilter;
import com.jspxcms.core.domain.Model;
import com.jspxcms.core.domain.ModelField;
import com.jspxcms.core.domain.Site;
import com.jspxcms.core.listener.ModelDeleteListener;
import com.jspxcms.core.listener.SiteDeleteListener;
import com.jspxcms.core.repository.ModelDao;
import com.jspxcms.core.service.ModelFieldService;
import com.jspxcms.core.service.ModelService;
import com.jspxcms.core.service.SiteService;
import com.jspxcms.core.support.DeleteException;

/**
 * ModelServiceImpl
 * 
 * @author liufang
 * 
 */
@Service
@Transactional(readOnly = true)
public class ModelServiceImpl implements ModelService, SiteDeleteListener {
	public List<Model> findList(Integer siteId, String type,
			Map<String, String[]> params, Sort sort) {
		return dao.findAll(spec(siteId, type, params), sort);
	}

	public RowSide<Model> findSide(Integer siteId, String type,
			Map<String, String[]> params, Model bean, Integer position,
			Sort sort) {
		if (position == null) {
			return new RowSide<Model>();
		}
		Limitable limit = RowSide.limitable(position, sort);
		List<Model> list = dao.findAll(spec(siteId, type, params), limit);
		return RowSide.create(list, bean);
	}

	private Specification<Model> spec(final Integer siteId, final String type,
			Map<String, String[]> params) {
		Collection<SearchFilter> filters = SearchFilter.parse(params).values();
		final Specification<Model> fsp = SearchFilter
				.spec(filters, Model.class);
		Specification<Model> sp = new Specification<Model>() {
			public Predicate toPredicate(Root<Model> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate pred = fsp.toPredicate(root, query, cb);
				if (siteId != null) {
					pred = cb.and(pred, cb.equal(root.get("site")
							.<Integer> get("id"), siteId));
				}
				if (StringUtils.isNotBlank(type)) {
					pred = cb.and(pred, cb.equal(root.get("type"), type));
				}
				return pred;
			}
		};
		return sp;
	}

	public List<Model> findList(Integer siteId, String type) {
		return dao.findList(siteId, type);
	}

	public Model findDefault(Integer siteId, String type) {
		return dao.findDefault(siteId, type);
	}

	public List<Model> findByNumbers(String[] numbers, Integer[] siteIds) {
		return dao.findByNumbers(numbers, siteIds);
	}

	public Model get(Integer id) {
		return dao.findOne(id);
	}

	@Transactional
	public Model save(Model bean, Integer siteId, Map<String, String> customs) {
		Site site = siteService.get(siteId);
		bean.setSite(site);
		bean.setCustoms(customs);
		bean.applyDefaultValue();
		bean = dao.save(bean);
		return bean;
	}

	@Transactional
	public Model copy(Integer oid, Model bean, Integer siteId,
			Map<String, String> customs) {
		save(bean, siteId, customs);
		if (oid != null) {
			Model obean = get(oid);
			if (obean != null) {
				ModelField field;
				for (ModelField ofield : obean.getFields()) {
					field = modelFieldService.copy(ofield, bean);
					bean.addField(field);
				}
			}
		}
		return bean;
	}

	@Transactional
	public Model clone(Model model, Integer siteId) {
		Model dest = new Model();
		BeanUtils.copyProperties(model, dest);
		dest.setFields(null);
		dest.setId(null);
		Map<String, String> mapDest = new HashMap<String, String>(
				model.getCustoms());
		save(dest, siteId, mapDest);

		List<ModelField> fields = model.getFields();
		if (fields != null) {
			List<ModelField> fieldDests = new ArrayList<ModelField>();
			ModelField fieldDest;
			for (ModelField field : fields) {
				fieldDest = modelFieldService.clone(field, dest);
				fieldDests.add(fieldDest);
			}
			dest.setFields(fieldDests);
		}
		return dest;
	}

	@Transactional
	public Model[] batchUpdate(Integer[] id, String[] name, String[] number) {
		Map<String, Integer> seqMap = new HashMap<String, Integer>();
		Model[] beans = new Model[id.length];
		for (int i = 0, len = id.length; i < len; i++) {
			beans[i] = get(id[i]);
			String type = beans[i].getType();
			Integer seq = seqMap.get(type);
			if (seq == null) {
				seq = 0;
				seqMap.put(type, seq);
			} else {
				seq++;
				seqMap.put(type, seq);
			}
			beans[i].setSeq(seq);
			beans[i].setName(name[i]);
			beans[i].setNumber(number[i]);
			update(beans[i], null);
		}
		return beans;
	}

	@Transactional
	public Model update(Model bean, Map<String, String> customs) {
		if (customs != null) {
			bean.getCustoms().clear();
			bean.getCustoms().putAll(customs);
		}
		bean.applyDefaultValue();
		bean = dao.save(bean);
		return bean;
	}

	private Model doDelete(Integer id) {
		Model entity = dao.findOne(id);
		if (entity != null) {
			dao.delete(entity);
		}
		return entity;
	}

	@Transactional
	public Model delete(Integer id) {
		firePreDelete(new Integer[] { id });
		return doDelete(id);
	}

	@Transactional
	public Model[] delete(Integer[] ids) {
		firePreDelete(ids);
		Model[] beans = new Model[ids.length];
		for (int i = 0; i < ids.length; i++) {
			beans[i] = doDelete(ids[i]);
		}
		return beans;
	}

	public void preSiteDelete(Integer[] ids) {
		if (ArrayUtils.isNotEmpty(ids)) {
			if (dao.countBySiteId(Arrays.asList(ids)) > 0) {
				throw new DeleteException("model.management");
			}
		}
	}

	private void firePreDelete(Integer[] ids) {
		if (!CollectionUtils.isEmpty(deleteListeners)) {
			for (ModelDeleteListener listener : deleteListeners) {
				listener.preModelDelete(ids);
			}
		}
	}

	private List<ModelDeleteListener> deleteListeners;

	@Autowired(required = false)
	public void setDeleteListeners(List<ModelDeleteListener> deleteListeners) {
		this.deleteListeners = deleteListeners;
	}

	private ModelFieldService modelFieldService;
	private SiteService siteService;

	@Autowired
	public void setModelFieldService(ModelFieldService modelFieldService) {
		this.modelFieldService = modelFieldService;
	}

	@Autowired
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private ModelDao dao;

	@Autowired
	public void setDao(ModelDao dao) {
		this.dao = dao;
	}
}
