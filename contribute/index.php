<?php include '../includes/config.php'; ?>
<?php
$bodyclass = "page";
$pageTitle = "Contribute - Apache Spot";
$description = "";
$currentPage = 'contribute';
?>
<?php include '../includes/header.php'; ?>
            
            <div id="content">
            	
            	<div class="wrap cf"><!--if page has sidebar, add class "with-sidebar"-->
            		<div class="main">
            			<h1 class="page-title">Proposed Apache-Spot (incubating) Commit Workflow</h1>
            			
            			<p><strong>NOTE: Most of this guide is based on ASF Documentation.</strong></p>
            			
            			<p>This guide is meant to provide a workflow for committers of Apache Spot. The proposed workflow is for using git with apache-spot codebase.</p>
            			
            			<p>Depending the nature of the change two different approaches can be used to commit to Apache Spot: <strong>Individual Push</strong> or <strong>Topic Branding</strong>.</p>
            			
            			<h3 class="center" style="margin-top:35px;">Individual Push (most commonly used by the community):</h3>
        			
            			<p><img src="<?php echo BASE_URL; ?>/library/images/individual-push.png" alt="" /></p>
            			
            			<p><strong>Steps:</strong></p>
            			
            			<ol>
            				<li>For the Github repository at <a href="https://github.com/apache/incubator-spot" target="_blank">https://github.com/apache/incubator-spot</a> if you haven't already. For more information about Fork please go to: <a href="https://help.github.com/articles/fork-a-repo/" target="_blank">https://help.github.com/articles/fork-a-repo/</a></li>
            				<li>Clone your fork, create a new branch named after a Jira issue (i.e. <strong>spot-100</strong>).</li>
            				<li>Push commits to your local branch.</li>
            				<li>Test it!!!</li>
            				<li>Create a pull request (PR) against the upstream repo (master) of apache-spot. For more information about how to create a pull request please go to: <a href="https://help.github.com/articles/about-pull-requests/" target="_blank">https://help.github.com/articles/about-pull-requests/</a>.</li>
            				<li>Wait for the maintainers to review your PR.</li>
            			</ol>
            			
            			<h3 class="center" style="margin-top:35px;">Topic Branching (upstream)</h3>
            			
            			<p>What are a topic branches?</p>
            			
            			<blockquote>According to the git definition: "<em>A topic branch is a short-lived branch that you create and use for a single particular feature or related work.</em>" (<a href="https://git-scm.com/book/en/v2/Git-Branching-Branching-Workflows#Topic-Branches" target="_blank">https://git-scm.com/book/en/v2/Git-Branching-Branching-Workflows#Topic-Branches</a>)</blockquote>
            			
            			<p>Sometimes a new major feature will have dependencies between modules or developers that can't be separated into individual pushes, when this happens, a topic branch will be created to deliver the complete functionality before the merge with the upstream (encapsulated dev enviroment).</p>
            			
            			<p>In order to create a topic branch, three requirements are needed:</p>
            			
            			<ol>
            				<li>A design document must be uploaded using Jira. This design must be approved by the maintainers.</li>
            				<li>A voting process will be required to approve the topic branch creation, at least 3 maintainers need to approve it.</li>
            				<li>A commitment to delete the branch after merging it into the upstream branch must be done. The topic branch should only exist while the work is still in progress.</li>
            			</ol>
            			
            			<p>A meaningful name must be given to the branch. It is recommended to use JIRA issue created with the design document to link the branch.</p>
            			
            			<p><img src="<?php echo BASE_URL; ?>/library/images/topic-branching.png" alt="" /></p>
            			
            			<p><strong>IMPORTANT: There shouldn't be a push without a Jira created previously</strong></p>
            			
            			<h3>Approvals and Voting Process:</h3>
            			
            			<blockquote>
            				<p>For code-modification, +1 votes are in favor of the proposal, but -1 votes are <u>vetos</u> and kill the proposal dead until all vetoers withdraw their -1 votes.</p> 
            				
            				<p>Unless a vote has been declared as using <u>lazy consensus</u>, three +1 votes are required for a code-modification proposal to pass.</p>
            				
            				<p>Whole numbers are recommended for this type of vote, as the opinion being expressed is Boolean: 'I approve/do not approve of this change.'</p>
            				
            				<p><strong>Source: <a href="http://apache.org/foundation/voting.html" target="_blank">http://apache.org/foundation/voting.html</a></strong></p>
        				</blockquote>
        				
        				<h3>Useful links:</h3>
        				
        				<ul>
        					<li><a href="https://www.apache.org/foundation/glossary.html" target="_blank">https://www.apache.org/foundation/glossary.html</a></li>
        					<li><a href="http://www.apache.org/dev/committers" target="_blank">http://www.apache.org/dev/committers</a></li>
        					<li><a href="http://www.apache.org/dev/git.html" target="_blank">http://www.apache.org/dev/git.html</a></li>
        					<li><a href="http://www.apache.org/dev/writable-git" target="_blank">http://www.apache.org/dev/writable-git</a></li>
        				</ul>
            		</div>
            	</div>
            	
            </div>


<?php include '../includes/prefooter.php'; ?>
<?php include '../includes/footer.php'; ?>