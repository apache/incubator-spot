#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
import md5
import api.resources.impala_engine as ImpalaEngine
import api.resources.hdfs_client as HDFSClient
from hdfs.util import HdfsError
import api.resources.configurator as Configuration
from collections import defaultdict
import json
import os

"""
--------------------------------------------------------------------------
Return list(dict) of all the connectios related to a request name in one hour
--------------------------------------------------------------------------
"""
def suspicious_requests(date,uri=None,ip=None,limit=250):

    db = Configuration.db()
    proxy_query = ("""
	SELECT STRAIGHT_JOIN
	    ps.tdate,ps.time,ps.clientip,ps.host,ps.reqmethod,ps.useragent,
        ps.resconttype,ps.duration,ps.username,ps.webcat,ps.referer,
        ps.respcode,ps.uriport,ps.uripath,ps.uriquery,ps.serverip,ps.scbytes,
        ps.csbytes,ps.fulluri,ps.ml_score,ps.uri_rep,ps.respcode_name,
        ps.network_context
	FROM
	    {0}.proxy_scores ps
	LEFT JOIN
	    {0}.proxy_threat_investigation pt
	    ON (ps.fulluri = pt.fulluri)
	WHERE
	    ps.y={1} AND ps.m={2} AND ps.d={3}
	    AND (pt.fulluri is NULL)
    """).format(db,date.year,date.month,date.day)


    p_filter = ""
    p_filter += " AND ps.fulluri LIKE '%{0}%'".format(uri) if uri else ""
    p_filter += " AND ps.clientip = '{0}'".format(ip) if ip else ""
    p_filter += " ORDER BY ps.ml_score limit {0}".format(limit)
    proxy_query = proxy_query + p_filter
    return ImpalaEngine.execute_query_as_list(proxy_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all the connectios details for one request.
--------------------------------------------------------------------------
"""
def details(date,uri,ip):

    if not uri and not ip:
        return None

    db = Configuration.db()
    p_details = ("""
		SELECT
		    tdate,time,clientIp,host,webcat,respcode,respcode_name
		    ,reqmethod,useragent,resconttype,referer,uriport,serverip
		    ,scbytes,csbytes,fulluri,hh
		FROM
		    {0}.proxy_edge
		WHERE
		    y={1} AND m={2} AND d={3} AND 
            (fulluri='{4}' AND clientIp='{5}')
		""").format(db,date.year,date.month,date.day,uri.replace("'","//'"),ip)
    return ImpalaEngine.execute_query_as_list(p_details)

"""
--------------------------------------------------------------------------
Score a request
--------------------------------------------------------------------------
"""
def score_request(date,score,uri):

    if not score and not uri:
	return None

    db = Configuration.db()
    p_query = ("""
		SELECT
		    tdate,time,clientip,host,reqmethod,useragent,resconttype
		    ,duration,username,webcat,referer,respcode,uriport
		    ,uripath,uriquery,serverip,scbytes,csbytes,fulluri
		    ,word,ml_score,uri_rep,respcode_name,network_context
		FROM
		    {0}.proxy_scores
		WHERE
		    y={1} and m={2} and d={3}
		    AND fulluri = '{4}'
		""").format(db,date.year,date.month,date.day,uri)

    connections = ImpalaEngine.execute_query(p_query)

    # add score to connections
    insert_command = ("""
		INSERT INTO {0}.proxy_threat_investigation PARTITION (y={1},m={2},d={3})
		VALUES (""") \
        .format(db,date.year,date.month,date.day)

    fb_data =  []
    first = True
    num_rows = 0
    for row in connections:
        cip_index = row[2]
        uri_index = row[18]
        tme_index = row[2]
        hash_field = [str( md5.new(str(cip_index) + str(uri_index)).hexdigest() \
        + str((tme_index.split(":"))[0]) )]

        threat_data = (row[0],row[18],score)
        fb_data.append([row[0],row[1],row[2],row[3],row[4],row[5],row[6],row[7] \
			,row[8],row[9],row[10],row[11],row[12],row[13],row[14],row[15] \
			,row[16],row[17],row[18],row[19],score,row[20],row[21],row[22], \
			row[23],hash_field])
        insert_command += "{0}{1}".format("," if not first else "", threat_data)
        first = False
        num_rows += 1

    insert_command += ")"
    if num_rows > 0: ImpalaEngine.execute_query(insert_command)

    # create feedback file.
    app_path = Configuration.spot()
    feedback_path = "{0}/proxy/scored_results/{1}{2}{3}/feedback"\
    .format(app_path,date.year,str(date.month).zfill(2),str(date.day).zfill(2))

    ap_file = True
    if len(HDFSClient.list_dir(feedback_path)) == 0:
    	fb_data.insert(0,["p_date","p_time","clientip","host","reqmethod",\
        "useragent","resconttype","duration","username","webcat","referer",\
        "respcode","uriport","uripath","uriquery","serverip","scbytes","csbytes",\
        "fulluri","word","score","uri_rep","uri_sev","respcode_name",\
        "network_context","hash"])
        ap_file = False

    HDFSClient.put_file_csv(fb_data,feedback_path,"ml_feedback.csv",append_file=ap_file)
    return True

"""
--------------------------------------------------------------------------
Get expanded search from raw table.
--------------------------------------------------------------------------
"""
def expanded_search(date,uri):

    db = Configuration.db()
    expanded_query = ("""
			SELECT p_date, p_time, clientip, username, duration, fulluri,\
			    webcat, respcode, reqmethod,useragent, resconttype,\
			    referer, uriport, serverip, scbytes, csbytes
			FROM {0}.proxy
			WHERE y='{1}' AND m='{2}' AND d='{3}'
			AND (fulluri='{4}' OR referer ='{4}')
			ORDER BY p_time
			""")\
            .format(db,date.year,str(date.month).zfill(2),str(date.day).zfill(2),uri)
    return ImpalaEngine.execute_query_as_list(expanded_query)

"""
--------------------------------------------------------------------------
Get scored request from threat investigation.
--------------------------------------------------------------------------
"""
def get_scored_requests(date):

    db = Configuration.db()
    sc_query =  ("""
                SELECT
                    tdate,fulluri,uri_sev
                FROM
                    {0}.proxy_threat_investigation
                WHERE
                    y={1} AND m={2} AND d={3}
                """).format(db,date.year,date.month,date.day)

    return ImpalaEngine.execute_query_as_list(sc_query)

"""
--------------------------------------------------------------------------
Create storyboard.
Migrated from IPython Notebooks
--------------------------------------------------------------------------
"""
def create_storyboard(uri,date,title,text,expanded_search,top_results):

    clientips  = defaultdict(int)
    reqmethods = defaultdict(int)
    rescontype = defaultdict(int)
    referers   = defaultdict(int)
    refered    = defaultdict(int)
    requests   = []


    for row in expanded_search:
        clientips[row['clientIp']]+=1
        reqmethods[row['requestMethod']]+=1
        rescontype[row['responseContentType']]+=1
        if row['uri'] == uri:
           #Source URI's that refered the user to the threat
           referers[row['referer']]+=1
           requests += [{'clientip':row['clientIp'], 'referer':row['referer'],'reqmethod':row['requestMethod'], 'resconttype':row['responseContentType']}]

        else:
            #Destination URI's refered by the threat
            refered[row['uri']]+=1

    create_incident_progression(uri,requests,refered,date)
    create_timeline(uri,clientips,date,top_results)
    save_comments(uri,title,text,date)

    return True

"""
--------------------------------------------------------------------------
Create timeline for storyboard
--------------------------------------------------------------------------
"""
def create_timeline(anchor,clientips,date,top_results):
    response = ""
    susp_ips = []

    if clientips:
        srtlist = sorted(list(clientips.items()), key=lambda x: x[1], reverse=True)
        for val in srtlist[:top_results]:
            susp_ips.append(val[0])

    if anchor != "":
        db = Configuration.db()
        time_line_query = ("""
                SELECT p_threat,tstart,tend,duration,clientip,respcode,respcodename
                FROM {0}.proxy_timeline
                WHERE
                    y={1} AND m={2} AND d={3} AND p_threat != '{4}'
                """).format(db,date.year,date.month,date.day,anchor.replace("'","//'"))
        
        tmp_timeline_data = ImpalaEngine.execute_query_as_list(time_line_query)

        imp_query = ("""
                        INSERT INTO TABLE {0}.proxy_timeline
                        PARTITION (y={2}, m={3},d={4})
                        SELECT
                            '{7}' as p_threat, concat(cast(p_date as string),
                            ' ', cast(MIN(p_time) as string)) AS tstart,
                            concat(cast(p_date as string), ' ',
                            cast(MAX(p_time) as string)) AS tend,
                            SUM(duration) AS duration,
                            clientip, respcode,"respCodeName" as respCodeName
                        FROM {0}.proxy
                        WHERE fulluri='{1}' AND clientip IN ({5})
                        AND y='{2}' AND m='{3}' AND d='{4}'
                        GROUP BY clientip, p_time, respcode, p_date
                        LIMIT {6}
                    """)\
                    .format(db,anchor,date.year,str(date.month).zfill(2),\
                    str(date.day).zfill(2),("'" + "','".join(susp_ips) + "'")\
                    ,top_results,anchor)

        app_path = Configuration.spot()
        old_file = "{0}/proxy/hive/oa/timeline/y={1}/m={2}/d={3}"\
        .format(app_path,date.year,date.month,date.day)

        HDFSClient.delete_folder(old_file,"impala")
        ImpalaEngine.execute_query("invalidate metadata")

        #Insert temporary values
        for item in tmp_timeline_data:
            insert_query = ("""
                        INSERT INTO {0}.proxy_timeline PARTITION(y={1} , m={2} ,d={3})
                        VALUES ('{4}', '{5}', '{6}',{7},'{8}','{9}','{10}')
                        """)\
                        .format(db,date.year,date.month,date.day,\
                        item["p_threat"],item["tstart"],item["tend"],item["duration"],item["clientip"],item["respcode"],item["respcodename"])

            ImpalaEngine.execute_query(insert_query)

        ImpalaEngine.execute_query(imp_query)
        response = "Timeline successfully saved"
    else:
        response = "Timeline couldn't be created"

"""
--------------------------------------------------------------------------
Create inciden progression for storyboard.
--------------------------------------------------------------------------
"""
def create_incident_progression(anchor,requests,referers,date):

    hash_name = md5.new(str(anchor)).hexdigest()
    file_name = "incident-progression-{0}.json".format(hash_name)
    app_path = Configuration.spot()
    hdfs_path = "{0}/proxy/oa/storyboard/{1}/{2}/{3}"\
    .format(app_path,date.year,date.month,date.day)

    data = {'fulluri':anchor, 'requests':requests,'referer_for':referers.keys()}
    if HDFSClient.put_file_json(data,hdfs_path,file_name,overwrite_file=True) :
        response = "Incident progression successfuly created"
    else:
        return False

"""
--------------------------------------------------------------------------
Save comments for storyboard.
--------------------------------------------------------------------------
"""
def save_comments(uri,title,text,date):

    db = Configuration.db()
    sb_query = ("""
            SELECT
               p_threat,title,text
            FROM
                {0}.proxy_storyboard
            WHERE
                y = {1} AND m= {2} AND d={3}
            """).format(db,date.year,date.month,date.day)
    sb_data = ImpalaEngine.execute_query_as_list(sb_query)


    # find value if already exists.
    saved = False
    for item in sb_data:
        if item["p_threat"] == uri:
            item["title"] = title
            item["text"] = text
            saved = True

    if not saved:
        sb_data.append({'text': text, 'p_threat': str(uri), 'title': title})

    #remove old file.
    app_path = Configuration.spot()
    old_file = "{0}/proxy/hive/oa/storyboard/y={1}/m={2}/d={3}/"\
    .format(app_path,date.year,date.month,date.day)
    HDFSClient.delete_folder(old_file,"impala")
    ImpalaEngine.execute_query("invalidate metadata")

    for item in sb_data:
        insert_query = ("""
                    INSERT INTO {0}.proxy_storyboard PARTITION(y={1} , m={2} ,d={3})
                    VALUES ( '{4}', '{5}', '{6}')
                    """)\
                    .format(db,date.year,date.month,date.day,\
                    item["p_threat"],item["title"],item["text"])

        ImpalaEngine.execute_query(insert_query)

"""
--------------------------------------------------------------------------
Get storyboard comments.
--------------------------------------------------------------------------
"""
def story_board(date):

    db = Configuration.db()
    sb_query= ("""
            SELECT
                p_threat,title,text
            FROM
                {0}.proxy_storyboard
            WHERE
                y={1} AND m={2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    results = ImpalaEngine.execute_query_as_list(sb_query)
    for row in results:
        row["text"] = row["text"].replace("\n","\\n")
    return results

"""
--------------------------------------------------------------------------
Get timeline for storyboard.
--------------------------------------------------------------------------
"""
def time_line(date,uri):

    db = Configuration.db()
    time_line_query = ("""
            SELECT
		p_threat,tstart,tend,duration,clientip,respcode,respcodename
            FROM {0}.proxy_timeline
            WHERE
                y={1} AND m={2} AND d={3}
                AND p_threat = '{4}'
            """).format(db,date.year,date.month,date.day,uri)

    return ImpalaEngine.execute_query_as_list(time_line_query)

"""
--------------------------------------------------------------------------
Get incident progression for storyboard.
--------------------------------------------------------------------------
"""
def incident_progression(date,uri):

    app_path = Configuration.spot()
    hdfs_path = "{0}/proxy/oa/storyboard/{1}/{2}/{3}".format(app_path,\
        date.year,date.month,date.day)

    hash_name = md5.new(str(uri)).hexdigest()
    file_name = "incident-progression-{0}.json".format(hash_name)

    if HDFSClient.file_exists(hdfs_path,file_name):
        return json.loads(HDFSClient.get_file("{0}/{1}"\
        .format(hdfs_path,file_name)))
    else:
        return {}

"""
Return a list(dict) with all the data ingested during the time frame provided.
"""
def ingest_summary(start_date,end_date):

    db = Configuration.db()
    is_query = ("""
                SELECT
                    tdate,total
                FROM {0}.proxy_ingest_summary
                WHERE
                    ( y >= {1} and y <= {2}) AND
                    ( m >= {3} and m <= {4}) AND
                    ( d >= {5} and d <= {6})
                """)\
                .format(db,start_date.year,end_date.year,start_date.month,end_date.month, start_date.day, end_date.day)

    return ImpalaEngine.execute_query_as_list(is_query)


"""
--------------------------------------------------------------------------
Reset scored connections.
--------------------------------------------------------------------------
"""
def reset_scored_connections(date):

    proxy_storyboard =  "proxy/hive/oa/storyboard"
    proxy_threat_investigation = "dns_threat_dendro/hive/oa/timeline"
    proxy_timeline = "proxy/hive/oa/threat_investigation"    
    app_path = Configuration.spot()   

    try:
        # remove parquet files manually to allow the comments update.
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,proxy_storyboard,date.year,date.month,date.day) , "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,proxy_threat_investigation,date.year,date.month,date.day), "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,proxy_timeline,date.year,date.month,date.day), "impala")
        ImpalaEngine.execute_query("invalidate metadata")
        return True

    except HdfsError:
        return False
