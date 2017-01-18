<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "page";
$pageTitle = "Machine Learning - Apache Spot";
$description = "";
$currentPage = 'machine-learning';
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
            	
            	<div class="wrap cf"><!--if page has sidebar, add class "with-sidebar"-->
            		<div class="main">
            			<h1 class="page-title">Apache Spot Machine Learning</h1>
            			            			
            			<p>The machine learning component of Apache Spot contains routines for performing suspicious connections analyses on netflow, DNS or proxy logs gathered from a network. These analyses consume a collection of network events and produce a list of the events that are considered to be the least probable, and these are consider the most suspicious.  They rely on the ingest component of Spot to collect and load netflow, DNS, and proxy records.</p>
            			
            			<p>Apache Spot uses topic modeling to discover normal and abnormal behavior. It treats the collection of logs related to an IP as a document and uses Latent Dirichlet Allocation (LDA) to discover hidden semantic structures in the collection of such documents.   </p>
            			
            			<p>LDA is a generative probabilistic model used for discrete data, such as text corpora. LDA is a three-level Bayesian model in which each word of a document is generated from a mixture of an underlying set of topics [1]. We apply LDA to network traffic by converting network log entries into words through aggregation and discretization. In this manner, documents correspond to IP addresses, words to log entries (related to an IP address) and topics to profiles of common network activity.</p>
            			
            			<p><img src="../../library/images/machine-learning.png" alt="" /></p>
            			
            			<p>Apache Spot infers a probabilistic model for the network behavior of each IP address. Each network log entry is assigned an estimated probability (score) by the model. The events with lower scores are flagged as “suspicious” for further analysis.</p>

						<p class="citation">[1] Blei, David M., Andrew Y. Ng, and Michael I. Jordan. "Latent dirichlet allocation." Journal of machine Learning research 3, no. Jan (2003): 993-1022.</p>
            		</div>
            		
					<!--<?php include '../../includes/download-sidebar.php';?>-->
					
            	</div>
            	
            </div>


<?php include '../../includes/prefooter.php'; ?>
<?php include '../../includes/footer.php'; ?>