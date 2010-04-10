/**
 *
 */
package com.google.code.magja.service.category;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;

import com.google.code.magja.magento.ResourcePath;
import com.google.code.magja.model.category.Category;
import com.google.code.magja.service.GeneralServiceImpl;
import com.google.code.magja.service.ServiceException;

/**
 * @author andre
 *
 */
public class CategoryRemoteServiceImpl extends GeneralServiceImpl<Category> implements CategoryRemoteService {

	/**
	 * Load children for the category
	 * @param category
	 * @throws ServiceException
	 */
	private void loadChildren(Category category) throws ServiceException {
		if (category.get("children") != null) {
			if (category.get("children").toString().length() > 0) {
				String str_children = (String) category.get("children");
				String[] arr_children = str_children.split(",");
				for (String str_child : arr_children) {
					Category child = getByIdClean(new Integer(str_child));
					if(child != null) category.addChild(child);
				}
			}
		}
	}

	/**
	 * load parent for the category
	 * @param category
	 * @throws ServiceException
	 */
	private void loadParent(Category category) throws ServiceException {
		if(category.get("parent_id") != null) {
			Category parent = getByIdClean((Integer) category.get("parent_id"));
			category.setParent(parent);
		}
	}

	/* (non-Javadoc)
	 * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdClean(java.lang.Integer)
	 */
	@Override
	public Category getByIdClean(Integer id) throws ServiceException {

		Category category = new Category();

		if(id == null) return null;

		Map<String, Object> cat;

		try {
			cat = (Map<String, Object>) soapClient.call(ResourcePath.CategoryInfo, id);
		} catch (AxisFault e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		if(cat == null) return null;

		for (Map.Entry<String, Object> attribute : cat.entrySet())
			category.set(attribute.getKey(), attribute.getValue());

		return category;
	}

	/* (non-Javadoc)
	 * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithChildren(java.lang.Integer)
	 */
	@Override
	public Category getByIdWithChildren(Integer id) throws ServiceException {

		Category category = getByIdClean(id);

		// load category children
		loadChildren(category);

		return category;
	}

	/* (non-Javadoc)
	 * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithParent(java.lang.Integer)
	 */
	@Override
	public Category getByIdWithParent(Integer id) throws ServiceException {

		Category category = getByIdClean(id);

		// load category parent
		loadParent(category);

		return category;
	}

	/* (non-Javadoc)
	 * @see com.google.code.magja.service.category.CategoryRemoteService#getByIdWithParentAndChildren(java.lang.Integer)
	 */
	@Override
	public Category getByIdWithParentAndChildren(Integer id) throws ServiceException {

		Category category = getByIdClean(id);

		// load category parent and children
		loadChildren(category);
		loadParent(category);

		return category;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.code.magja.service.category.CategoryRemoteService#save(code
	 * .google .magja.model.product.Category)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int save(Category category) throws ServiceException {
		if (category.getId() == null) {
			// means its a new category
			try {
				List<Object> newProduct = (LinkedList<Object>) category.serializeToApi();

				Integer id = Integer.parseInt((String) soapClient.call(ResourcePath.CategoryCreate, newProduct));
				if (id > -1) {
					category.setId(id);

					return id;
				} else {
					throw new ServiceException("Error inserting new Category");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			} catch (AxisFault e) {
				e.printStackTrace();
				throw new ServiceException(e.getMessage());
			}
		} else {
			// TODO implement the update category
			return category.getId();
		}
	}

	/**
	 * Delete a category by id
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void delete(Integer id) throws ServiceException {
		Boolean success = false;
		try {
			success = (Boolean) soapClient.call(ResourcePath.CategoryDelete, id);
		} catch (AxisFault e) {
			System.out.println(e.getMessage());
			throw new ServiceException(e.getMessage());
		}
		if (!success) {
			throw new ServiceException("Not success deleting category.");
		}
	}

	/**
	 * Delete a delete all children in a category
	 * 
	 * @param parent
	 *            category id
	 * @throws ServiceException
	 */
	public void deleteAllChildren(Integer id) throws ServiceException {
		Category parent = getByIdWithChildren(id);
		List<Category> children = parent.getChildren();

		for (Category category : children) {
			delete(category.getId());
		}
	}

	/**
	 * get default root category
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public Category getDefaultParent() throws ServiceException {
		return getByIdClean(soapClient.getConfig().getDefaultRootCategoryId());
	}

	/**
	 * create category tree from String
	 */
	public int create(Integer parentId, String categoryName) throws ServiceException {
		return create(parentId, new String[] { categoryName });
	}

	/**
	 * create category tree from String array
	 */
	public int create(Integer parentId, String[] categoryNames) throws ServiceException {
		for (int i = 0; i < categoryNames.length; i++) {
			if (categoryNames[i].length() > 0) {
				Category parent = getByIdWithChildren(parentId);
				Category newCategory = null;

				// check if children already exists
				boolean childrenFound = false;
				if (parent.getChildren() != null) {
					for (int c = 0; c < parent.getChildren().size() && !childrenFound; c++) {
						Category children = parent.getChildren().get(c);
						if (categoryNames[i].equals(children.getName())) {
							childrenFound = true;
							newCategory = children;
						}
					}
				}

				if (!childrenFound) {
					// children not found, create new one
					newCategory = new Category(parent, categoryNames[i]);
				}

				parentId = save(newCategory);
			}
		}

		return parentId;
	}
	
}
