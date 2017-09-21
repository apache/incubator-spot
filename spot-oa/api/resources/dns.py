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
import api.resources.impala_engine as ImpalaEngine
import api.resources.hdfs_client as HDFSClient
import api.resources.configurator as Configuration
from hdfs.util import HdfsError

"""
--------------------------------------------------------------------------
Return list(dict) of all the suspicious dns connections in one day.
--------------------------------------------------------------------------
"""
def suspicious_queries(date, ip=None, query=None,limit=250):

    db = Configuration.db()
    sq_query = ("""
            SELECT STRAIGHT_JOIN
                ds.unix_tstamp,frame_len,ds.ip_dst,ds.dns_qry_name,
                dns_qry_class,dns_qry_type,dns_qry_rcode,ml_score,tld,
                query_rep,hh,dns_qry_class_name,dns_qry_type_name,
                dns_qry_rcode_name,network_context
            FROM {0}.dns_scores ds
            LEFT JOIN {0}.dns_threat_investigation dt
                ON  (ds.dns_qry_name = dt.dns_qry_name)
            WHERE
                ds.y={1} AND ds.m={2} AND ds.d={3}
                AND (dt.dns_qry_name is NULL)
            """).format(db,date.year,date.month,date.day)

    sq_filter = ""
    sq_filter += " AND ds.ip_dst = '{0}'".format(ip) if ip else ""
    sq_filter += " AND ds.dns_qry_name LIKE '%{0}%'".format(query) if query else ""
    sq_filter += " ORDER BY ds.ml_score limit {0}".format(limit)

    sq_query = sq_query + sq_filter
    return ImpalaEngine.execute_query_as_list(sq_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all the connectios related to a query name in one hour
--------------------------------------------------------------------------
"""
def details(frame_time, query):

    db = Configuration.db()
    details_query = ("""
            SELECT
		unix_tstamp,frame_len,ip_dst,ip_src,dns_qry_name,dns_qry_class,
		dns_qry_type,dns_qry_rcode,dns_a,dns_qry_type_name,
		dns_qry_rcode_name,dns_qry_class_name
            FROM
                {0}.dns_edge
            WHERE
                y={1} AND m={2} AND d={3} AND hh={4} AND dns_qry_name = '{5}'
            """).format(db,frame_time.year,frame_time.month,frame_time.day,\
            frame_time.hour,query)

    return ImpalaEngine.execute_query_as_list(details_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all the connections with a single client.
--------------------------------------------------------------------------
"""
def client_details(date, ip):

    db = Configuration.db()
    client_query =("""
            SELECT
                ip_dst,dns_a,dns_qry_name,ip_dst
            FROM
                {0}.dns_dendro
            WHERE
                y={1} AND m={2} AND d={3}
                AND ip_dst='{4}'
            """).format(db,date.year,date.month,date.day,ip)

    return ImpalaEngine.execute_query_as_list(client_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all the detected suspicious connections in one day.
--------------------------------------------------------------------------
"""
def incident_progression(date, query,ip):

    if not ip and not query:
        return None

    db = Configuration.db()
    return_value = "dns_qry_name" if ip else "ip_dst"
    dns_threat_query = ("""
            SELECT
                anchor,total,{0}
            FROM
                {1}.dns_threat_dendro
            WHERE
                y={2} AND m={3} AND d={4}
                AND anchor = '{5}'
            """).format(return_value,db,date.year,date.month,date.day,\
            query if query else ip)
                
    return ImpalaEngine.execute_query_as_list(dns_threat_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all queries saved in threat investigation.
--------------------------------------------------------------------------
"""
def comments(date):

    db = Configuration.db()
    comments_query = ("""
            SELECT
                ip_threat,dns_threat,title,text
            FROM
                {0}.dns_storyboard
            WHERE
                y={1} AND m={2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    results = ImpalaEngine.execute_query_as_list(comments_query)
    for row in results:
        row["text"] = row["text"].replace("\n","\\n")
    return results

"""
--------------------------------------------------------------------------
Score connections.
--------------------------------------------------------------------------
"""
def  score_connection(date,ip="", dns="", ip_sev=0, dns_sev=0):

    if (not ip and not ip_sev) and (not dns and not dns_sev):
        return False

    db = Configuration.db()
    sq_query = ("""
		SELECT
    	    frame_time,unix_tstamp,frame_len,ip_dst,dns_qry_name,dns_qry_class,
		    dns_qry_type,dns_qry_rcode,ml_score,tld,query_rep,
		    hh,dns_qry_class_name,dns_qry_type_name,dns_qry_rcode_name,
		    network_context
		FROM
		    {0}.dns_scores
		WHERE
		    y={1} and m={2} and d={3}
            AND (
		""").format(db,date.year,date.month,date.day)

    connections_filter = ""
    connections_filter += "ip_dst = '{0}' ".format(ip) if ip else ""
    connections_filter += " OR " if ip and dns else ""
    connections_filter += "dns_qry_name = '{0}' ".format(dns) if dns else ""
    connections_filter += ")"
    connections = ImpalaEngine.execute_query(sq_query + connections_filter)

    # add score to connections

    insert_command = ("""INSERT INTO {0}.dns_threat_investigation
                        PARTITION (y={1},m={2},d={3})
                        VALUES (""") \
                        .format(db,date.year,date.month,date.day)

    fb_data =  []
    first = True
    num_rows = 0
    for row in connections:
        # insert into dns_threat_investigation.
        threat_data = (row[1],row[3],row[4],ip_sev if ip == row[3] else 0,\
        dns_sev if dns == row[4] else 0)

        fb_data.append([row[0],row[2],row[3],row[4],row[5],row[6],row[7],\
        row[8],row[9],row[10],row[11],ip_sev,dns_sev,row[12],row[13],row[14],\
        row[15],row[1]])

        insert_command += "{0}{1}".format("," if not first else "", threat_data)
        first = False
        num_rows += 1

    insert_command += ")"
    if num_rows > 0: ImpalaEngine.execute_query(insert_command)

    # create feedback file.
    app_path = Configuration.spot()
    feedback_path = "{0}/dns/scored_results/{1}{2}{3}/feedback"\
    .format(app_path,date.year,str(date.month).zfill(2),str(date.day).zfill(2))
    ap_file = True

    if len(HDFSClient.list_dir(feedback_path)) == 0:
        fb_data.insert(0,["frame_time","frame_len","ip_dst","dns_qry_name",\
        "dns_qry_class","dns_qry_type","dns_qry_rcode","score","tld","query_rep",\
        "hh","ip_sev","dns_sev","dns_qry_class_name","dns_qry_type_name",\
        "dns_qry_rcode_name","network_context","unix_tstamp"])
        ap_file = False

    HDFSClient.put_file_csv(fb_data,feedback_path,"ml_feedback.csv",append_file=ap_file)
    return True

"""
--------------------------------------------------------------------------
Return list(dict) of all the scored connections in one day.
--------------------------------------------------------------------------
"""
def  get_scored_connections(date):

    db = Configuration.db()
    sc_query =  ("""
                SELECT
                    unix_tstamp,ip_dst,dns_qry_name,ip_sev,dns_sev
                FROM
                    {0}.dns_threat_investigation
                WHERE
                    y={1} AND m={2} AND d={3}
                """).format(db,date.year,date.month,date.day)

    return ImpalaEngine.execute_query_as_list(sc_query)

"""
--------------------------------------------------------------------------
Get expanded search from raw data table.
--------------------------------------------------------------------------
"""
def expanded_search(date,query=None,ip=None,limit=20):

    if not ip and not query:
        return False

    db = Configuration.db()
    if ip:
	count = "dns_qry_name"
        filter_param = "ip_dst"
	filter_value = ip
    else:
	count = "ip_dst"
	filter_param = "dns_qry_name"
	filter_value = query

    expanded_query = ("""
   		SELECT
    		    COUNT({0}) as total,dns_qry_name,ip_dst
		FROM
		    {1}.dns
		WHERE y={2} AND m={3} AND d={4}
		AND {5} = '{6}'
		GROUP BY {0},{5}
		ORDER BY total DESC
		LIMIT {7}
    """).format(count,db,date.year,date.month,date.day,\
    filter_param,filter_value,limit if limit else 20)

    return ImpalaEngine.execute_query_as_list(expanded_query)

"""
--------------------------------------------------------------------------
Create StoryBoard.
--------------------------------------------------------------------------
"""
def create_storyboard(expanded_search,date,ip,query,title,text):

    if not ip and not query:
        return False

    anchor = ip if ip else query
    create_dendro(expanded_search,date,anchor)
    save_comments(anchor,ip,query,title,text,date)

"""
--------------------------------------------------------------------------
Create dendrogram for StoryBoard.
--------------------------------------------------------------------------
"""
def create_dendro(expanded_search,date,anchor):

    db = Configuration.db()
    for row in expanded_search:
	dendro_query = ("""
		INSERT INTO {0}.dns_threat_dendro PARTITION (y={1}, m={2},d={3})
		VALUES ( '{4}',{5},'{6}','{7}')
		""")\
        .format(db,date.year,date.month,date.day,anchor,\
        row["total"],row["dnsQuery"],row["clientIp"])

	ImpalaEngine.execute_query(dendro_query)

"""
--------------------------------------------------------------------------
Create save comments for StoryBoard.
--------------------------------------------------------------------------
"""
def  save_comments(anchor,ip,query,title,text,date):

    db = Configuration.db()
    sb_query = ("""
            SELECT
                ip_threat,dns_threat,title,text
            FROM
                {0}.dns_storyboard
            WHERE
                y = {1} AND m= {2} AND d={3}
            """).format(db,date.year,date.month,date.day)
    sb_data = ImpalaEngine.execute_query_as_list(sb_query)

    # find value if already exists.
    saved = False
    for item in sb_data:
        if item["ip_threat"] == anchor or item["dns_threat"]== anchor:
            item["title"] = title
            item["text"] = text
            saved = True

    if not saved:
        sb_data.append({'text': text, 'ip_threat': str(ip), 'title': title,'dns_threat':query})


    #remove old file.
    app_path = Configuration.spot()
    old_file = "{0}/dns/hive/oa/storyboard/y={1}/m={2}/d={3}/"\
    .format(app_path,date.year,date.month,date.day)

    HDFSClient.delete_folder(old_file,"impala")
    ImpalaEngine.execute_query("invalidate metadata")

    for item in sb_data:
	insert_query = ("""
         	INSERT INTO {0}.dns_storyboard PARTITION(y={1} , m={2} ,d={3})
            	VALUES ( '{4}', '{5}', '{6}','{7}')
            	""")\
                .format(db,date.year,date.month,date.day,\
                item["ip_threat"],item["dns_threat"],item["title"],item["text"])
        ImpalaEngine.execute_query(insert_query)

    return True

"""
--------------------------------------------------------------------------
Return a list(dict) with all the data ingested during the timeframe
provided.
--------------------------------------------------------------------------
"""
def ingest_summary(start_date,end_date):

    db = Configuration.db()
    is_query = ("""
                SELECT
                    tdate,total
                FROM {0}.dns_ingest_summary
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

    dns_storyboard =  "dns/hive/oa/storyboard"
    dns_threat_investigation = "dns_threat_dendro/hive/oa/threat_dendro"
    dns_timeline = "dns/hive/oa/threat_investigation"    
    app_path = Configuration.spot()   

    try:
        # remove parquet files manually to allow the comments update.
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,dns_storyboard,date.year,date.month,date.day) , "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,dns_threat_investigation,date.year,date.month,date.day), "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,dns_timeline,date.year,date.month,date.day), "impala")
        ImpalaEngine.execute_query("invalidate metadata")
        return True

    except HdfsError:
        return False
