<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "page";
$pageTitle = "Open Data Models - Apache Spot";
$description = "";
$currentPage = 'open-data-models';
?>
<?php include '../../includes/header.php'; ?>

			<!--
            <div id="masthead">
                <div class="wrap cf">
                    <div class="m-all d-1of2 right-center">
						<h1>Lorem ispum dolor sit amet, consectetur adipisicing elit</h1>
                    </div>
                </div>
            </div>-->

            
            <div id="content">
            	
            	<div class="wrap cf with-sidebar"><!--if page has sidebar, add class "with-sidebar"-->
            		<div class="main">
            			<h1 class="page-title">Apache Spot Open Data Model</h1>
            			           			
            			<p>Many organizations have built threat detection capabilities leveraging myriad vendor solutions.  This approach leads to many silos of data corresponding to each vendor and often results in storing multiple copies of the same data, as each vendor’s capability operates independently from the others. There is no single vendor able to cost-effectively store and analyze all the data required to detect threats and facilitate incident investigations and remediation.</p>
            			
            			<p>Apache Spot ODM brings together all security-related data (event, user, network, endpoint, etc.) into a singular view that can be used to detect threats more effectively than ever before.  This consolidated view can be leveraged to create new analytic models that were not previously possible and to provide needed context at the event level to effectively determine whether or not there is a threat.  The Apache Spot ODM enables the sharing and reuse of threat detection models, algorithms and analytics, because of a shared, open data model.</p>
            			
            			<p>The open data model (ODM) provides a common taxonomy for describing security telemetry data used to detect threats. It uses schemas, data structures, file formats and configurations in the underlying CDH platform for collecting, storing and analyzing security telemetry data at scale. Spot defines relationships amongst the various security data types for joining log data with user, network and endpoint entity data.</p>
            			
            			<p>The Apache Spot ODM enables organizations to:</p>
            			<ul>
            				<li>Store one copy of the security telemetry data and apply UNLIMITED analytics
            					<ul>
            						<li>Leverage out-of-the-box analytics powered by machine learning to detect threats in DNS, Flow and Proxy data</li>
            						<li>Build custom analytics to your desired specification</li>
            						<li>Plug-in third-party vendor analytics that interoperate with the ODM</li>
            					</ul>	
        					</li>
            				<li>Share and/or reuse threat detection models, algorithms, ingest pipelines, visualizations and analytics across the Apache Spot community, due to a common data model.</li>
            				<li>Leverage all your security telemetry data to establish the context needed to better detect threats
            					<ul>
            						<li>Security logs</li>
            						<li>User, endpoint and network entity data</li>
            						<li>Threat intelligence data</li>
            					</ul>	
        					</li>
        					<li>Avoid “lock-in” to a specific technology and gain needed analytic flexibility resultant from a shared, open data model.</li>
            			</ul>
            		</div>
            		
					<!--<?php include '../../includes/download-sidebar.php';?>-->
					
            	</div>
            	
            </div>

<?php include '../../includes/prefooter.php'; ?>
<?php include '../../includes/footer.php'; ?>