<?php include '../../includes/config.php'; ?>
<?php
$bodyclass = "page";
$pageTitle = "Visualization - Apache Spot";
$description = "";
$currentPage = 'visualization';
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
            			<h1 class="page-title">Visualization</h1>
            			<h3>Take advantage of Apache Spot's tools to perform further analysis over the suspicious activity detected by our machine learning algorithm</h3>
            			
            			<h4>Suspicious</h4>
            			
            			<p>Study <strong>suspicious</strong> network activity by looking at a list of security threats detected by Apache Spot's machine learning algorithm.</p>
            			
            			<p><img src="../../library/images/suspicious.png" alt="" /></p>
            			
            			<p>Have a nice view of your network, understand how devices interact with each other and easily spot threats while exploring a visual representation of suspicious activity.</p>
            			
            			<p><img src="../../library/images/network.png" alt="" /></p>
            			
            			<p>The following feature is powered by IPython notebooks which allows the users to switch back and forth from the 'easy mode' to the 'expert mode', where they can view and edit the code behind this panel via the web browser.</p>
            			
            			<p>In the 'Notebook' panel, the form displayed is where the user can assign the level of risk for each connection and use that as feedback to train the Machine Learning model in future executions. Switching to the 'expert' mode, the user can adjust the criteria to filter the data, discarding results known to be non relevant to the analysis.</p>
            			
            			<p><img src="../../library/images/notebook.png" alt="" /></p>
            			
            			<p>As your investigation moves forward, get <strong>detailed</strong> information about a threat whenever you want to dig into an especific threat.</p>
            			
            			<p><img src="../../library/images/details.png" alt="" /></p>
            			
            			<h4>Threat Investigation</h4>
            			
            			<p>The threat investigation panel represents the last step of analysis before displaying the storyboard. At this point, the security analysts can enter a custom review for a given threat to display.</p>
            			
            			<h4>Storyboard</h4>
            			
            			<p>Ready to present your findings? Go over your high risk security threats and request further information, making it easy for executives to undestand what is going on. Here is a list of some of the information you will get when your analyses comes to the end.</p>
            			
            			<ul>
            				<li>Incident Progression</li>
            				<li>Impact Analysis</li>
            				<li>Geographic location</li>
            				<li>Incident Timeline</li>
            			</ul>
            			
            			<h4>Ingest Summary</h4>
            			
            			<p>Wondering about how much data have been ingested on your cluster? We provide a nice visualization which allows you to get this information.</p>
            			
            			<p><img src="../../library/images/ingest-summary.png" alt="" /></p>
            			
            			<p>The "scoring panel" as well as the "Threat investigation panel" are powered by Jupyter notebooks, <a href="https://jupyter-notebook-beginner-guide.readthedocs.io/en/latest/what_is_jupyter.html" target="_blank">(click here to learn more)</a>.</p>
            		</div>
            		
					<!--<?php include '../../includes/download-sidebar.php';?>-->
					
            	</div>
            	
            </div>


<?php include '../../includes/prefooter.php'; ?>
<?php include '../../includes/footer.php'; ?>