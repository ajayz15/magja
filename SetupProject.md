# Setup developer environment #


---

### Install Eclipse ###
#### On Ubuntu: ####
  1. Since Ubuntu Lucid Lynx 10.04 you have to add Canonical Partner repository "deb http://archive.canonical.com/ubuntu lucid partner" to your /etc/apt/sources.list first to by able to install sun-java6-jdk.
  1. Go **Applicationes > Accessories > Terminal** and run this command: **sudo apt-get install -y eclipse sun-java6-jdk**

#### On Windows / Mac: ####
  1. Install **Java Standard Edition JDK 6** first: http://www.oracle.com/technetwork/java/javase/downloads/
  1. Download and install **Eclipse IDE for Java Developers** package from http://www.eclipse.org/downloads/ homepage.

### Install Maven Plugin for Eclipse ###
  1. Start Eclipse, then go to **Help** > **Install New Software**. This should display the "Install" dialog.
  1. Paste following URL http://m2eclipse.sonatype.org/sites/m2e into the field named **Work with:** and press Add button.
  1. Confirm the "Add Site" dialog with OK. This should cause Eclipse to update list of available plugins and components.
  1. Choose **"Maven Integration for Eclipse"** and press "Next >". Eclipse will then check to see if there are any issues which would prevent a successful installation.
  1. Click **Next >** and agree to the terms of the Eclipse Public License v1.0.
  1. Click **Finish** to begin the installation process. Eclipse will then download and install the necessary components.
  1. If the "Do you trust those certificates" dialog appears, click **Select All** and then **OK** button
  1. Confirm "Software Updates" dialog with **Yes** to restart your Eclipse IDE

Source: http://m2eclipse.sonatype.org/installing-m2eclipse.html

### Install Subversive Plugin for Eclipse ###
  1. Select **Help** > **Install New Software**. This should display the "Install" dialog.
  1. Paste following URL http://subclipse.tigris.org/update_1.6.x into the field named **Work with:** and press Add button.
  1. Confirm the "Add Site" dialog with OK. This should cause Eclipse to update list of available plugins and components.
  1. Choose **"Core SVNKit Library, Optional JNA Library, Subclipse"** and press "Next >".
  1. Once you have installed the subversive, go to "SVN Repository Exploring" perspective, click on the "New Repository Location" button (left-up).
  1. Confirm "Install Details" with **Next >**
  1. On the "Review Licenses" page select "I accept the terms of the license agreements" then click on **Finish** button.
  1. Confirm "Security Warning" with **OK**
  1. If the "Do you trust those certificates" dialog appears, click **Select All** and then **OK** button
  1. Confirm "Software Updates" dialog with **Yes** to restart your Eclipse IDE

### Import Magja SVN ###
  1. The "New Repository Location" will pop-up, fill the URL field with the following http://magja.googlecode.com/svn/, then click Finish.
  1. The new location will appear on left side of your perspective, just right-click on trunk folder and choose "Checkout As New Project"

Source: http://www.eclipse.org/subversive/documentation/gettingStarted/aboutSubversive/install.php

### Enable Project Dependency Management ###

Once you had check out the project, right-click on the project, the sub-menu will pop-up, then go to **Maven** > **Enable Dependency Management**. Wait a little until all dependencies be downloaded to your maven repository.

To learn more about maven go to: http://maven.apache.org/index.html

### Change the Magento Connection Properties File ###

Change or create the file /src/main/resources/magento-api.properties with yout proper data to magento installation, for example:

```
# connection parameters
magento-api-username=yourMagentoApiUser
magento-api-password=youtMagentoApiPassword
magento-api-url=http://localhost/magento/index.php/api/soap/

# the ID of the default attribute set
default-attribute-set-id=4

# the ID of the default root category
default-root-category-id=2
```


For test if you can connect to magento API, run the "testConnectionLogin" method of the class "ConnectionTest" as JUnit Test.

### Extend Magento Catalog Api ###

The Magento Core API doesn't have methods to manipulate some features wich are required for Magja. So you have to install a Magento Module to provide that's features, all you have to do is download the Magja Magento Extension Module from http://code.google.com/p/magja/downloads/detail?name=MagjaCatalogExt.zip

### The SVN Repository ###

The SVN repository is divided in two projects, one its the Magja Core, wich are the core's Java Project, the other its on the magja-catalog-ext folder, wich is a sub-project layout: the Magento Extension Module, it is for Magento instances where you want to use with Magja.


---
