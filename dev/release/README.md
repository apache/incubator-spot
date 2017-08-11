# **Apache Spot (Incubating) Release Process**

## **Overview**  

This document describes the Release Process to perform the official Apache Spot (Incubating) release following the [Apache Software Foundation Release Policy](http://incubator.apache.org/guides/releasemanagement.html#best-practice). 
 
### **Requirements**

As a Release Manager (RM), you should create a code signing key to sign the release artifacts following this [guide](http://www.apache.org/dev/openpgp.html#generate-key).

Public key should be detached and added in to the KEYS file in Spot Repo under: https://dist.apache.org/repos/dist/dev/incubator/spot/KEYS

### **Policy documents**

Frequently asked questions for making Apache releases are available on [Releases FAQ page](http://www.apache.org/legal/release-policy.html#releases).

The Release Manager must go through the policy document to understand all the tasks and responsibilities of running a release.

### **Give a heads up**

The release manager should create an EPIC in Jira and then setup a timeline for release branch point. The time for the day the EPIC is created to the release branch point must be at least two weeks in order to give the community a chance to prioritize and commit any last minute features and issues they would like to see in the upcoming release.

The release manager should then send the pointer to the EPIC along with the tentative timeline (Code Freeze) for branch point to the user and developer lists. Any work identified as release related that needs to be completed should be added as a subtask of the umbrella issue to allow users to see the overall release progress in one place.

    To: dev@spot.apache.org

    Subject: Work on Spot <your release name>  (Incubating) Release has started + Code Freeze Date


    We are starting the process to prepare for Spot <your release name> (Incubating) release. I have opened JIRA $jira to cover the features included in this release.

    If you have any JIRA in progress and would like to include in this release, please follow the process to do so. Code Freeze for final integration will be on $code_freeze_date.

    Feel free to comment on the JIRA if you have any comments/suggestions.

    Thanks,
    <Release Manager Name>

### **Sanitize Jira**

Before a release is done, make sure that any issues that are fixed have their fix version setup correctly. If the release number is not listed in the "fix version" field, as RM create a ticket for Infrastructure asking to create the value for the "fix version" field. 

Once the value is created by Infrastructure team, run the following JIRA query to see which resolved issues do not have their fix version set up correctly:

project = spot and resolution = fixed and fixVersion is empty

The result of the above query should be empty. If some issues do show up in this query that have been fixed since the last release, please bulk-edit them to set the fix version to '1.0'.

You can also run the following query to make sure that the issues fixed for the to-be-released version look accurate:

project = spot and resolution = fixed and fixVersion = '1.0'

### **Monitor active issues**

It is important that between the time that the umbrella issue is filed to the time when the release branch is created, no experimental or potentially destabilizing work is checked into the trunk. While it is acceptable to introduce major changes, they must be thoroughly reviewed and have good test coverage to ensure that the release branch does not start of being unstable.

If necessary the RM can discuss if certain issues should be fixed on the trunk in this time, and if so what is the gating criteria for accepting them.

### **Pull Request Validation**

All the features that will be included in the release EPIC needs to have a proper Pull Request (PR) created following this guide. And needs three +1 votes which at least one of them must be from the QA Team. 

Development must be done in to a Topic Branch or Master Branch, depending on the scope of the release.

Once the PR has votes, then the PPMC developer must merge the PR and the owner of that PR should close it properly in Github (in case it does not close automatically).

If Development is performed in Topic Branch, then Topic Branch should be merged in to master branch once development is done. 

### **Create the Release Candidate**

The Release candidate branch will be created from Master branch once all pull requests form the EPIC are merged to a topic branch or directly to master branch before code freeze date.

#### **Branch your release:**

* git checkout -b `<your release name>` `<commit sha1>` 

push to origin:

* git push origin `<your release name>`

#### **Tag your Branch release:**
Apply signed tag on release branch that will indicate where the release candidate was generated.

Example:
* git tag -u `<GPG KEY ID>` --sign `<your release name>`-incubating -m "Apache Spot `<your release name>` (Incubating)" `<SHA of HEAD of branch>`

### **Run RAT**
Apache Rat is a release audit tool, focused on licenses. Used to improve accuracy and efficiency when checking releases for licenses.

Download RAT: 
* wget http://apache.claz.org//creadur/apache-rat-0.12/apache-rat-0.12-bin.tar.gz

Decompress the code:
* tar -zxvf apache-rat-0.12-bin.tar.gz
* cd apache-rat-0.12

Now lets create the file to exclude the known extensions and files:
* vi or nano .rat-excludes

Add the following exclude list:

    .*md
    .*txt
    .gitignore
    .gitmodules
    .*png
    .*json
    .*csvss
    .*less
    .*ipynb
    .babelrc
    topojson.min.js

Save the File.

Or download the .rat-excludes files from: https://github.com/apache/incubator-spot/blob/master/dev/release/.rat-excludes

Run the rat tool as following.
* java -jar apache-rat-0.12.jar -E /path/to/project/.rat-excludes -d /path/to/project/ > `<to output file>`.txt

If you have rat in the same directory as the Spot Code you can verify as:
* java -jar apache-rat-0.12.jar -E .rat-excludes -d ../apache-spot-1.0-incubating > apache-spot-1.0-incubating-rat-results.txt

If RAT find problems in the licenses please fix the licence and run RAT again developers must fix their code and submit changes into the release branch, once there are no more findings. Upload RAT Results into subversion dev incubator repo for Spot of the release

#### **Make a tarball and gzip:**
* git archive -o ../apache-spot-`<your release name>`-incubating.tar --prefix=apache-spot-`<your release name>`-incubating/ `<your tag/branch name>`
* gzip ../apache-spot-`<your release name>`-incubating.tar

Example:

    $ git archive -o ../apache-spot-1.0-incubating.tar --prefix=apache-spot-1.0-incubating/ 1.0-incubating
    $ gzip ../apache-spot-1.0-incubating.tar

#### **Prepare MD5, SHA512 and ASC files from the source tarball:**

* md5 apache-spot-`<your release name>`-incubating.tar.gz > apache-spot-`<your release name>`-incubating.tar.gz.md5
* shasum -a 512 apache-spot-`<your release name>`-incubating.tar.gz > apache-spot-`<your release name>`-incubating.tar.gz.sha512 
* gpg2 --detach-sign -a apache-spot-`<your release name>`-incubating.tar.gz

Example:

    $ md5 apache-spot-1.0-incubating..tar.gz > apache-spot-1.0-incubating.tar.gz.md5
    $ shasum -a 512 apache-spot-1.0-incubating.tar.gz > apache-spot-1.0-incubating.tar.gz.sha512
    $ gpg2 --detach-sign -a apache-spot-1.0-incubating..tar.gz
 

#### **Retrieve the subversion dev incubator repo for Spot**

Example:
* svn checkout https://dist.apache.org/repos/dist/dev/incubator/spot/ --username=`<your apache user>`
 
Create a local folder for the release (e.g. 1.0-incubating) in svn. 
* svn mkdir -m "Creating Spot `<release number>` dir" https://dist.apache.org/repos/dist/dev/incubator/spot/`<release number>` --username=`<your apache user>`

Example:

    svn mkdir -m "Creating Spot 1.0-incubating dir" https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incuabting --username=`<your apache user>`


Move the files into the release folder on local disk.
* svn add `<release folder>`

Example:

    svn add 1.0-incubating/

Commit artifacts:
* svn commit -m '`<custom message>`' --username=`<your apache user id>`

Example:

    svn commit -m 'adding spot 1.0-incubating candidate release artifacts' --username=`<your apache user id>`

## **Validate the Build**

Download the tarball.

* http://spot.apache.org/download

Decompress the tarball. Instruction:
    
    tar -zxvf apache-spot-1.0-incubating.tar.gz

Change directory. Instruction:
    
    cd apache-spot-1.0-incubator

Apache Spot (incubating) is composed of more than one module or sub-projects. Since some of them are Python or Javascript code, they don’t need compilation.

For more instructions about how to install each module please read below instructions.

 You should see the content of the folder:

    spotadmin-mac01:apache-spot-1.0-incubating spotadmin$ ls -la
    total 72
    drwxr-xr-x 14 spotadmin staff 476 Jul 24 16:45 .
    drwxr-xr-x 7 spotadmin staff 238 Aug 4 09:32 ..
    -rw-r--r-- 1 spotadmin staff 20 Jul 24 16:45 .gitignore
    -rw-r--r-- 1 spotadmin staff 0 Jul 24 16:45 .gitmodules
    -rw-r--r-- 1 spotadmin staff 560 Jul 24 16:45 DISCLAIMER
    -rw-r--r-- 1 spotadmin staff 11918 Jul 24 16:45 LICENSE
    -rw-r--r-- 1 spotadmin staff 1493 Jul 24 16:45 LICENSE-topojson.txt
    -rw-r--r-- 1 spotadmin staff 159 Jul 24 16:45 NOTICE
    -rw-r--r-- 1 spotadmin staff 6761 Jul 24 16:45 README.md
    drwxr-xr-x 3 spotadmin staff 102 Jul 24 16:45 docs
    drwxr-xr-x 10 spotadmin staff 340 Jul 24 16:45 spot-ingest
    drwxr-xr-x 13 spotadmin staff 442 Jul 24 16:45 spot-ml
    drwxr-xr-x 11 spotadmin staff 374 Jul 24 16:45 spot-oa
    drwxr-xr-x 10 spotadmin staff 340 Jul 24 16:45 spot-setup

Decompressed tarball content should be the same with the content located in: 

* [https://github.com/apache/incubator-spot/tree/v1.0-incubating](https://github.com/apache/incubator-spot/tree/v1.0-incubating)

To install the properly component please follow this guide:

* [http://spot.apache.org/doc/#installation](http://spot.apache.org/doc/#installation)

Spot Ingest, Spot Setup, Spot OA and Spot UI have specific requirements to install manually.
    
* [http://spot.apache.org/doc/#configuration](http://spot.apache.org/doc/#configuration)
* [http://spot.apache.org/doc/#ingest](http://spot.apache.org/doc/#ingest)
* [http://spot.apache.org/doc/#oa](http://spot.apache.org/doc/#oa)
* [http://spot.apache.org/doc/#ui](http://spot.apache.org/doc/#ui)

Spot ML is the only component to build the binary files using sbt assembly commands. Please follows these instructions.
    
* [http://spot.apache.org/doc/#ml](http://spot.apache.org/doc/#ml)


## **Running the Vote**

As per the Apache Incubator release [guidelines](http://incubator.apache.org/policy/incubation.html#Releases), all releases for incubating projects must go through a two-step voting process. First, release voting must successfully pass within the Apache Spot (Incubating) community via the dev@spot.incubator.apache.org mail list. Then, release voting must successfully pass within the Apache Incubator PMC via the general@incubator.apache.org mail list.

### **Call for Spot Community Vote**

Call for Vote in spot dev community sending an email to dev list.

For example,

    To: dev@spot.apache.org
    Subject: [VOTE] Release Apache Spot 1.0-incubating

    Hi All, 

    This is the vote for Apache Spot 1.0 (incubating) release.

    The vote will run for at least 72 hours and will close on July 27,2017.

    Release Notes (Jira generated):
    https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12320637&version=12340668

    Git Branch and Tag for the release:
    https://github.com/apache/incubator-spot/tree/branch-1.0
    https://github.com/apache/incubator-spot/tree/v1.0-incubating

    Source code for the release:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz
    
    Source release verification:
    PGP Signature:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.asc

    MD5/SHA512 Hash:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.md5
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.sha512

    RAT license Verification:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating-rat-results.txt 

    Keys to verify the signature of the release artifact are available at:
    https://dist.apache.org/repos/dist/dev/incubator/spot/KEYS

    The artifact(s) have been signed with Key : 06B82CAEDB5B280349E75D5533CD9431141E946C

    Download the release candidate and evaluate the necessary items including checking hashes, signatures, source code and test.
    Please vote accordingly:
    [ ] +1 approve
    [ ] +0 no opinion
    [ ] -1 disapprove (and reason why)

    =================
    DISCLAIMER

    Apache Spot (incubating) is an effort undergoing incubation at the Apache Software Foundation (ASF), sponsored by the Apache Incubator PMC.
    Incubation is required of all newly accepted projects until a further review indicates that the infrastructure, communications, and decision making process have stabilized in a manner consistent with other successful ASF projects.

    While incubation status is not necessarily a reflection of the completeness or stability of the code, it does indicate that the project has yet to be fully endorsed by the ASF.
    =================

    --
    Best Regards!
    -----------------------------------
    <Release Manager Name>
    http://spot.apache.org/
    -----------------------------------

Allow the community to vote and any -1 vote with comments please fix ASAP and send findings to the thread until the required votes are reached by the PPMC members of Spot.

Send a following email with the results that should include the counting votes and thread of the results listed in http://lists.apache.org

    To: dev@spot.apache.org
    Subject: [RESULT][VOTE] Release Apache Spot 1.0-incubating

    Hi All, 

    The voting process for the Release Apache Spot 1.0-incubating is now closed with the following and positive results:

    [10] Binding Votes
    [1] Non-binding

    Thread of the voting email with responses can be found here:

    https://lists.apache.org/thread.html/69dfe2626c7b803e2a3f26e4d348be8d1941003f0e8166fb8e0e9679@%3Cdev.spot.apache.org%3E

    The next step will be sending the release artifacts for voting at the Incubator General list to get the IPMC approval to officially declare the release. 


    Thanks
    --
    Best Regards!
    -----------------------------------
    <Release Manager Name>
    http://spot.apache.org/
    -----------------------------------

### **Call for Incubator PMC Vote**

The second voting is the most important since it is required to get three +1 (Binding) vote from the IPMC members from the Incubator General list to declare an official release.

Send the vote to the general Incubator list and include the voting results from the dev list as evidence. 

    To: general@incubator.apache.org
    Subject: [VOTE] Release Apache Spot 1.0-incubating

    Dear IPMC team,

    This is the vote for Apache Spot 1.0 (incubating) release. This is the first release of Spot.

    Apache Spot (Incubating) is open source software for leveraging insights from flow and packet analysis. It helps enterprises and service providers gain insight on their network environments through transparency of service delivery and identification of potential security threats or attacks happening among resources operating at cloud scale. While current threat intelligence tools help, identifying unknown threats and attacks remains a challenge. Apache Spot provides tools to accelerate companies’ ability to expose suspicious connections and previously unseen attacks using flow and packet analysis technologies.

    The PPMC Vote Threads can be found here:
    https://lists.apache.org/thread.html/69dfe2626c7b803e2a3f26e4d348be8d1941003f0e8166fb8e0e9679@%3Cdev.spot.apache.org%3E


    The PPMC vote results can be found here:
    https://lists.apache.org/thread.html/a88ef44e0dcda9013781eeca363ad9b3439f6c34a698c6eaa50fb314@%3Cdev.spot.apache.org%3E 


    Release Notes (Jira generated):
    https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12320637&version=12340668

    Git Branch and Tag for the release:
    https://github.com/apache/incubator-spot/tree/branch-1.0
    https://github.com/apache/incubator-spot/tree/v1.0-incubating

    Source code for the release:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz
    
    Source release verification:
    http://nolamarketing.com/client/apache-spot/download/

    PGP Signature:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.asc

    MD5/SHA512 Hash:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.md5
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating.tar.gz.sha512
    
    RAT license Verification:
    https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incubating/apache-spot-1.0-incubating-rat-results.txt 

    Keys to verify the signature of the release artifact are available at:
    https://dist.apache.org/repos/dist/dev/incubator/spot/KEYS

    The artifact(s) have been signed with Key : 06B82CAEDB5B280349E75D5533CD9431141E946C

    Download the release candidate and evaluate the necessary items.

    Please vote accordingly:
    [ ] +1, approve as the official Apache Spot 1.0-incubating release
    [ ] -1, do not accept as the official as the official Apache Spot 1.0-incubating release because...

    The vote will run for at least 72 hours or until necessary number of votes are reached.

    =================
    DISCLAIMER

    Apache Spot (incubating) is an effort undergoing incubation at the Apache Software Foundation (ASF), sponsored by the Apache Incubator PMC.
    Incubation is required of all newly accepted projects until a further review indicates that the infrastructure, communications, and decision making process have stabilized in a manner consistent with other successful ASF projects.

    While incubation status is not necessarily a reflection of the completeness or stability of the code, it does indicate that project has yet to be fully endorsed by the ASF.
    =================

    --
    Best Regards!
    -----------------------------------
    <Release Manager Name>
    http://spot.apache.org/
    -----------------------------------

Monitor the voting thread and make sure you have the required votes, if feedback is provides fix ASAP and send the updates to the voting thread so voting reaches the required votes.

Once we have the three +1 (Binding) votes then send the following email with the results:


    To: general@incubator.apache.org
    Subject: [RESULT][VOTE] Release Apache Spot 1.0-incubating

    Hi All, 

    The voting process for the Release Apache Spot 1.0-incubating is now closed with the following and positive results:

    [3] Binding Votes
    [1] Non-binding

    Thread of the voting email with responses can be found here:

    https://lists.apache.org/thread.html/32d7c93fe66cc256ed12a5b8f91b57b1d0d659b9012c8f4f13c11191@%3Cgeneral.incubator.apache.org%3E

    
    I will prepare the artifacts to officially release Apache Spot 1.0-incubating. 


    Thanks
    --
    Best Regards!
    -----------------------------------
    <Release Manager Name>
    http://spot.apache.org/
    -----------------------------------


Moving the Artifacts to release stage in SVN using the following command.
* svn move -m "`<comment>`" `<Directory Origin>` `<Directory Destination>` --username=`<your apache user id>`

Example:
    
    svn move -m "Moving Apache Spot 1.0-incubating release artifacts to release stage" /* https://dist.apache.org/repos/dist/dev/incubator/spot/1.0-incuabting https://dist.apache.org/repos/dist/release/incubator/spot/1.0-incuabting --username=`<your apache user id>`

Allow 24 hours before updating the webpage and announcing the new Release in Apache Spot (Incubating) webpage. http://nolamarketing.com/client/apache-spot/download/

### **Update WebPages**

You need to update the Spot webpages to reflect the new release.

### **Announce the release**

Email to the different distribution lists announce@apache.org, user@spot.apache.org, dev@spot.apache.org (using your @apache.org email) For example:

    To: announce@apache.org, user@spot.apache.org, dev@spot.apache.org
    Subject: [ANNOUNCE] Apache Spot 1.0 (incubating) released

    The Apache Spot (Incubating) team is pleased to announce the release of Spot 1.0-incubating.

    This is the first release of Spot. Major step forward of the project.

    Apache Spot (Incubating) is open source software for leveraging insights from flow and packet analysis. It helps enterprises and service providers gain insight on their network environments through transparency of service delivery and identification of potential security threats or attacks happening among resources operating at cloud scale. While current threat intelligence tools help, identifying unknown threats and attacks remains a challenge.

    The release is available here:
    http://nolamarketing.com/client/apache-spot/download/ (Update from final version)

    The full change log is available here:
    https://issues.apache.org/jira/secure/ReleaseNote.jspa?projectId=12320637&version=12340668

    Your help and feedback is more than welcome. For more information on how to report problems and to get involved, visit the project website at http://spot.apache.org/.

    The Apache Spot (Incubating) Team


### **Close the Jira Ticket**

Once the release is announced you can go to Jira and Close the EPIC created to perform the release.