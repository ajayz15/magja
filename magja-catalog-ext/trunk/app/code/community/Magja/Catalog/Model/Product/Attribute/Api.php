<?php

/**
 * Magja Catalog product attribute api extension
 *
 * @category   Magja
 * @package    Magja_Catalog
 * @author     Magja Core Team - http://magja.googlecode.com
 */
class Magja_Catalog_Model_Product_Attribute_Api extends Mage_Catalog_Model_Product_Attribute_Api {
	
	/**
	 * Retrieve product attribute info by code
	 *
	 * @param string $attributeCode
	 * @param string|int $store
	 * @return array
	 */
	public function info($attributeCode, $store = null) {
		
		$storeId = $this->_getStoreId ( $store );
		$attribute = Mage::getModel ( 'catalog/product' )->setStoreId ( $storeId )->getResource ()->getAttribute ( $attributeCode )->setStoreId ( $storeId );
		
		if (!$attribute) {
            $this->_fault('not_exists');
        }
		
		if (! $attribute->getId () || $attribute->isScopeGlobal ()) {
			$scope = 'global';
		} elseif ($attribute->isScopeWebsite ()) {
			$scope = 'website';
		} else {
			$scope = 'store';
		}
		
		//$result = print_r($attribute, true);
		
        $result = array(
        	'attribute_id' 	=> $attribute->getId(),
            'code'        	=> $attribute->getAttributeCode(),
            'type'       	=> $attribute->getFrontendInput(),
            'backend' 		=> $attribute->getData('backend_type'),
            'frontend'   	=> $attribute->getFrontendLabel(),
        	'label'   		=> $attribute->getFrontendLabel(),
        	'class'   		=> $attribute->getData('frontend_class'),
        	'default'   	=> $attribute->getData('default_value'),
        	'visible'   	=> $attribute->getIsVisible(),
        	'required'   	=> $attribute->getData('is_required'),
        	'user_defined'  => $attribute->getData('is_user_defined'),
        	'searchable'   	=> $attribute->getIsSearchable(),
        	'filterable'   	=> $attribute->getIsFilterable(),
        	'comparable'   	=> $attribute->getIsComparable(),
        	'visible_on_front' => $attribute->getData('is_visible_on_front'),
        	'visible_in_advanced_search' => $attribute->getIsVisibleInAdvancedSearch(),
        	'unique'		=> $attribute->getData('is_unique'),
        	'used_for_sort_by' => $attribute->getUsedForSortBy(),
        	'sortBy' 		=> $attribute->getUsedForSortBy(),
        	'scope'        	=> $scope,
        	'is_configurable' => $attribute->getIsConfigurable(),
        );
        
		return $result;
	}
	
	/**
	 * Create new product attribute.
	 *
	 * @param string $attributeName
	 * @param array $attributeData
	 * @param string|int $store
	 * @return int
	 */
	public function create($attributeName, $attributeData, $store = null) {
		// create product attribute
		$installer = new Mage_Catalog_Model_Resource_Eav_Mysql4_Setup ( 'core_setup' );
		$installer->addAttribute ( 'catalog_product', $attributeName, $attributeData );
		
		// get product attribute id
		$storeId = $this->_getStoreId ( $store );
		$attribute = Mage::getModel ( 'catalog/product' )->setStoreId ( $storeId )->getResource ()->getAttribute ( $attributeName );
		
		return $attribute->getId ();
	}
	
	/**
	 * Create attribute options
	 *
	 * @param string $attributeId
	 * @param array $attributeOptions
	 * @return int
	 */
	public function addoptions($attributeId, $attributeOptions) {
		$setup = new Mage_Eav_Model_Entity_Setup ( 'core_setup' );
		
		for($i = 0; $i < sizeof ( $attributeOptions ); $i ++) {
			$option = array ();
			$option ['attribute_id'] = $attributeId;
			$option ['value'] [$value] [0] = $attributeOptions [$i];
			
			$setup->addAttributeOption ( $option );
		}
		
		return true;
	}
	
	/**
	 * Delete product attribute.
	 *
	 * @param string $attributeName
	 * @param string|int $store
	 * @return int
	 */
	public function delete($attributeName, $store = null) {
		$storeId = $this->_getStoreId ( $store );
		$attribute = Mage::getModel ( 'catalog/product' )->setStoreId ( $storeId )->getResource ()->getAttribute ( $attributeName );
		
		if (! $attribute) {
			$this->_fault ( 'not_exists' );
		}
		
		try {
			$attribute->delete ();
		} catch ( Mage_Core_Exception $e ) {
			$this->_fault ( 'not_deleted', $e->getMessage () );
			
			return false;
		}
		
		return true;
	}

}
?>