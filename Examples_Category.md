## Category ##

Get only the basic information of a Category, without its dependencies:
```
Category category = RemoteServiceFactory.getCategoryRemoteService().getByIdClean(ID_CATEGORY);
System.out.println(category.toString());
```

Get category with its children:
```
Category category = RemoteServiceFactory.getCategoryRemoteService().getByIdWithChildren(ID_CATEGORY);
for (Category child : category.getChildren()) {
    System.out.println(child.toString());
}
```

Get category with its parent:
```
Category category = RemoteServiceFactory.getCategoryRemoteService().getByIdWithParent(ID_CATEGORY);
System.out.println(category.getParent().toString());
```

Get category with its parent and children:
```
Category category = RemoteServiceFactory.getCategoryRemoteService().getByIdWithParentAndChildren(ID_CATEGORY);
```

Create category with translation:
```
// create default category
Category catEn = RemoteServiceFactory.getCategoryRemoteService().create(2, "Category 1");

// build translation category
Category catDe = new Category();
catDe.setId(catEn.getId());
catDe.setActive(true);
catDe.setName("Kategorie 1");
 
// save translation
RemoteServiceFactory.getCategoryRemoteService().save(catDe, "de");
```

## Category Attributes ##
Get information about the attributes of the store's categories. If a category attribute its of the type "select" or "multiselect", then its maybe has options (  [CategoryAttributeOption](http://code.google.com/p/magja/source/browse/trunk/src/main/java/code/google/magja/model/category/CategoryAttributeOption.java) ).

List all category attributes:
```
List<CategoryAttribute> categoryAttributes = RemoteServiceFactory.getCategoryAttributeRemoteService().testListAll();
for (CategoryAttribute categoryAttribute : categoryAttributes) System.out.println(categoryAttribute.toString());
```