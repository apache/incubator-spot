# **Apache Spot (incubating)**
Apache Spot (incubating) Operational Analytics (OA) is a collection of modules, which includes both the data processing and transformation as well as the GUI module for data visualization.

The visualization repository (UI folder) contains all the front-end code and files related to the Open Network Insight visual elements, such as styles, pages, data files, etc.
Some of the technologies used are:

 - [IPython==3.2.1](https://ipython.org/ipython-doc/3/index.html)
 - [D3js](http://d3js.org/)
 - [JQuery](https://jquery.com/)
 - [Bootstrap](http://getbootstrap.com/)
 - [ReactJS](https://facebook.github.io/react/)  

** For more specific requirements, please refer to each specific pipeline readme file before running OA.*
----------

## **Installation**

1. Install python dependencies `pip install -r requirements.txt`
2. Install UI requirements and build UI following the steps from [here](ui/INSTALL.md)

## **Folder Structure**

spot-oa is the root folder, below are more details about child folders:

 - [**context**](/spot-oa/context/README.md) : Static files for adding network context to the data  
 - [**oa**](/spot-oa/oa/INSTALL.md) : Operational Analytics path
 - [**ui**](/spot-oa/ui/README.md) : GUI files

----------

## **Operational Analytics (Back end)**
* [DNS](/spot-oa/oa/dns/README.md)
* [FLOW](/spot-oa/oa/flow/README.md)
* [PROXY](/spot-oa/oa/proxy/README.md)

## **IPython Notebooks**
* Flow
 * [EdgeNotebook](/spot-oa/oa/flow/ipynb_templates/EdgeNotebook.md)
 * [ThreatInvestigation](/spot-oa/oa/flow/ipynb_templates/ThreatInvestigation.md)
* DNS
 * [EdgeNotebook](/spot-oa/oa/dns/ipynb_templates/EdgeNotebook.md)
 * [ThreatInvestigation](/spot-oa/oa/dns/ipynb_templates/ThreatInvestigation.md)
* PROXY
 * [EdgeNotebook](/spot-oa/oa/proxy/ipynb_templates/EdgeNotebook.md)
 * [ThreatInvestigation](/spot-oa/oa/proxy/ipynb_templates/ThreatInvestigation.md)

## **Views**

### Analyst View (suspicious.html)
The Analyst view provides a dashboard showing the top 250 suspicious connects.

### **Threat Investigation (threat-investigation.html)**
HTML page container for the Threat Investigation Notebook

### **Story Board (storyboard.html)**
Executive Briefing dashboard

### **Ingest Summary (ingest-summary.html)**
Interactive histogram dashboard that shows the amount of data captured in a certain period of time.
