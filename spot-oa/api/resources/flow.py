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
import os,csv
import linecache,bisect
import struct, socket
from hdfs.util import HdfsError
import json
import numpy as np

"""
--------------------------------------------------------------------------
Return a list (dict) of all the suspicious connections that happened
during the specified timeframe.
--------------------------------------------------------------------------
"""
def suspicious_connections(date,ip=None,limit=250):

    db = Configuration.db()
    sc_query = ("""
                SELECT STRAIGHT_JOIN
                    fs.tstart,fs.srcip,fs.dstip,fs.sport,fs.dport,proto,
                    ipkt,ibyt,opkt,obyt,ml_score,rank,srcip_internal,
                    dstip_internal,src_geoloc,dst_geoloc,src_domain,
                    dst_domain,src_rep,dst_rep
                FROM {0}.flow_scores fs
                LEFT JOIN {0}.flow_threat_investigation ft
                    ON (( fs.srcip = ft.srcip) OR ( fs.dstip = ft.dstip))
                WHERE fs.y={1} AND fs.m={2} and fs.d={3}
                    AND ( ft.srcip is NULL AND ft.dstip is NULL )
                """).format(db,date.year,date.month,date.day)

    sc_filter = ""
    if ip:
        sc_filter = " AND ( fs.srcip='{0}' OR fs.dstip='{0}')".format(ip)

    sc_filter += " ORDER BY rank  limit {0}".format(limit)
    sc_query = sc_query + sc_filter
    return ImpalaEngine.execute_query_as_list(sc_query)

"""
--------------------------------------------------------------------------
Retuarn a list(dict) of all the connections that happened
between 2 IPs in one minute.
--------------------------------------------------------------------------
"""
def details(src_ip,dst_ip,date):

    db = Configuration.db()
    details_query = ("""
            SELECT
                tstart,srcip,dstip,sport,dport,proto,flags,
                tos,ibyt,ipkt,input,output,rip,obyt,opkt
            FROM {0}.flow_edge
            WHERE
                y={1} AND m={2} AND d={3} AND hh={4} AND mn={5}
                AND ((srcip='{6}' AND dstip='{7}')
                OR  (srcip='{7}' AND dstip='{6}'))
            ORDER BY tstart
            """).format(db,date.year,date.month,date.day,date.hour, \
                        date.minute,src_ip,dst_ip)

    return ImpalaEngine.execute_query_as_list(details_query)

"""
--------------------------------------------------------------------------
Return list(dict) of all the connections related with a single IP.
--------------------------------------------------------------------------
"""
def chord_details(ip,date):

    db = Configuration.db()
    chord_query =  ("""
            SELECT
                srcip,dstip,ibyt,ipkt
            FROM {0}.flow_chords
            WHERE  y={1} AND m={2} AND d={3} AND ip_threat='{4}'
            """).format(db,date.year,date.month,date.day,ip)

    return ImpalaEngine.execute_query_as_list(chord_query)

"""
--------------------------------------------------------------------------
Return a list(dict) with all the data ingested during
the timeframe provided.
--------------------------------------------------------------------------
"""
def ingest_summary(start_date,end_date):

    db = Configuration.db()
    is_query = ("""
            SELECT
                tdate,total
            FROM {0}.flow_ingest_summary
            WHERE
                ( y >= {1} AND y <= {2}) AND
                ( m >= {3} AND m <= {4}) AND
                ( d >= {5} AND d <= {6})
            ORDER BY tdate
            """).format(db,start_date.year,end_date.year, \
                        start_date.month,end_date.month, \
                        start_date.day, end_date.day)

    return ImpalaEngine.execute_query_as_list(is_query)

"""
--------------------------------------------------------------------------
Return list(dict) of connecions that happened between 2 ip
grouped by second.
--------------------------------------------------------------------------
"""
def time_line(ip,date):

    db = Configuration.db()
    time_line_query = ("""
        SELECT
            ip_threat,tstart,tend,srcip,dstip,proto,
		    sport,dport,ipkt,ibyt
        FROM {0}.flow_timeline
        WHERE
            y={1} AND m={2} AND d={3}
            AND ip_threat = '{4}'
        """).format(db,date.year,date.month,date.day,ip)

    return ImpalaEngine.execute_query_as_list(time_line_query)

"""
--------------------------------------------------------------------------
Return json file with all the  geo localization information.
--------------------------------------------------------------------------
"""
def sc_geo(ip,date):

    app_path = Configuration.spot()
    file_name = "globe-{0}.json".format(ip.replace(".","_"))
    hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
    .format(app_path,date.year,date.month,date.day,ip.replace(".","_"))

    if HDFSClient.file_exists(hdfs_path,file_name):
        return json.loads(HDFSClient.get_file("{0}/{1}" \
        .format(hdfs_path,file_name)))
    else:
        return {}

"""
--------------------------------------------------------------------------
Return a list(dict) with the ip threatn information captured by the
security expert.
--------------------------------------------------------------------------
"""
def story_board(date):

    db = Configuration.db()
    sb_query= ("""
            SELECT
                ip_threat,title,text
            FROM
                {0}.flow_storyboard
            WHERE
                y={1} AND m={2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    results = ImpalaEngine.execute_query_as_list(sb_query)
    for row in results:
	       row["text"] = row["text"].replace("\n","\\n")
    return results

"""
--------------------------------------------------------------------------
Return a json file with the impact analysis information.
--------------------------------------------------------------------------
"""
def impact_analysis(ip,date):

    app_path = Configuration.spot()
    file_name = "stats-{0}.json".format(ip.replace(".","_"))
    hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
    .format(app_path,date.year,date.month,date.day,ip.replace(".","_"))

    if HDFSClient.file_exists(hdfs_path,file_name):
        return json.loads(HDFSClient.get_file("{0}/{1}" \
        .format(hdfs_path,file_name)))
    else:
        return {}

"""
--------------------------------------------------------------------------
Return a list(dict) with all the inbound, outbound and twoway connections.
--------------------------------------------------------------------------
"""
def incident_progression(ip,date):

    app_path = Configuration.spot()
    file_name = "threat-dendro-{0}.json".format(ip.replace(".","_"))

    hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
    .format(app_path,date.year,date.month,date.day,ip.replace(".","_"))

    if HDFSClient.file_exists(hdfs_path,file_name):
        return json.loads(HDFSClient.get_file("{0}/{1}" \
        .format(hdfs_path,file_name)))
    else:
        return {}

"""
--------------------------------------------------------------------------
Save scored connection into Threat investigation table.
--------------------------------------------------------------------------
"""
def score_connection(score,date,src_ip=None,dst_ip=None,src_port=None,dst_port=None):

    if not src_ip and not dst_ip and not src_port and not dst_port:
        return False

    db = Configuration.db()
    # get connections to score
    connections_query = ("""
            SELECT
                tstart,srcip,dstip,sport,dport, ibyt,ipkt
            FROM {0}.flow_scores
            WHERE
                y = {1} AND m={2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    connections_filter = ""
    connections_filter += " AND srcip = '{0}'".format(src_ip) if src_ip else ""
    connections_filter += " AND dstip = '{0}'".format(dst_ip) if dst_ip else ""

    connections_filter += " AND sport = {0}" \
    .format(str(src_port)) if src_port else ""

    connections_filter += " AND dport = {0}" \
    .format(str(dst_port)) if dst_port else ""
    connections = ImpalaEngine.execute_query(connections_query + connections_filter)


    # add score to connections
    insert_command = ("""
        INSERT INTO {0}.flow_threat_investigation
        PARTITION (y={1},m={2},d={3})
        VALUES (""") \
        .format(db,date.year,date.month,date.day)

    fb_data =  []
    first = True
    num_rows = 0
    for row in connections:
        # insert into flow_threat_investigation.
        threat_data = (row[0],row[1],row[2],row[3],row[4],score)
        fb_data.append([score,row[0],row[1],row[2],row[3],row[4],row[5],row[6]])
        insert_command += "{0}{1}".format("," if not first else "", threat_data)
        first = False
        num_rows += 1

    insert_command += ")"
    if num_rows > 0: ImpalaEngine.execute_query(insert_command)

    # create feedback file.
    app_path = Configuration.spot()
    feedback_path = "{0}/flow/scored_results/{1}{2}{3}/feedback" \
    .format(app_path,date.year,str(date.month).zfill(2),str(date.day).zfill(2))

    append_file = True
    if len(HDFSClient.list_dir(feedback_path)) == 0:
        fb_data.insert(0,["sev","tstart","sip","dip","sport","dport","ipkt","ibyt"])
        append_file = False

    HDFSClient.put_file_csv(fb_data,feedback_path,"ml_feedback.csv",\
    append_file=append_file)
    return True

"""
--------------------------------------------------------------------------
Save connections details to flow_storyboard table.
--------------------------------------------------------------------------
"""
def save_comment(ip,title,text,date):

    #Get current table info.
    db = Configuration.db()
    sb_query = ("""
            SELECT
                ip_threat,title,text
            FROM
                {0}.flow_storyboard
            WHERE
                y = {1} AND m= {2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    sb_data = ImpalaEngine.execute_query_as_list(sb_query)

    # find value if already exists.
    saved = False
    for item in sb_data:
        if item["ip_threat"] == ip:
            item["title"] = title
            item["text"] = text
            saved = True

    if not saved:
        sb_data.append({'text': text, 'ip_threat': str(ip), 'title': title})

    #remove old file.
    app_path = Configuration.spot()
    old_file = "{0}/flow/hive/oa/storyboard/y={1}/m={2}/d={3}/" \
    .format(app_path,date.year,date.month,date.day)

    # remove file manually to allow the comments update.
    HDFSClient.delete_folder(old_file,"impala")
    ImpalaEngine.execute_query("invalidate metadata")

    for item in sb_data:
	insert_query = ("""
         	INSERT INTO {0}.flow_storyboard PARTITION(y={1} , m={2} ,d={3})
            	VALUES ( '{4}', '{5}','{6}')
            	""") \
                .format(db,date.year,date.month,date.day, \
                item["ip_threat"],item["title"],item["text"])

        ImpalaEngine.execute_query(insert_query)
    return True

"""
--------------------------------------------------------------------------
Get scored connections from threat investigation table.
--------------------------------------------------------------------------
"""
def get_scored_connections(date):


    db = Configuration.db()
    scored_query = ("""
            SELECT
                tstart,srcip,dstip,srcport,dstport,score
            FROM
                {0}.flow_threat_investigation
            WHERE
                y={1} AND m={2} AND d={3}
            """).format(db,date.year,date.month,date.day)

    return ImpalaEngine.execute_query_as_list(scored_query)

"""
--------------------------------------------------------------------------
Get expanded search data from raw data table.
--------------------------------------------------------------------------
"""
def expanded_search(date,ip):

    db = Configuration.db()
    expanded_query = ("""
		SELECT
		    min(treceived) as firstseen, max(treceived) as lastseen,
            sip as srcip, dip as dstip, sport as sport,
            dport as dport, count(sip) as conns, max(ipkt) as maxpkts,
		    avg(ipkt) as avgpkts, max(ibyt) as maxbyts, avg(ibyt) as avgbyts
	    FROM
		    {0}.flow
        WHERE
	        y={1} AND m={2} AND d={3}
        AND (sip ='{4}'  OR dip='{4}')
        GROUP BY
		    sip, dip,sport,dport
		""").format(db,date.year,date.month,date.day,ip)

    return ImpalaEngine.execute_query_as_list(expanded_query)

"""
--------------------------------------------------------------------------
Generates all the required data for StoryBoard.
--------------------------------------------------------------------------
"""
def create_storyboard(expanded_search,date,ip,title,text,top_results=20):


    cpath = "{0}/context/" \
    .format(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

    iploc = "{0}/{1}".format(cpath,'iploc.csv')
    nwloc = "{0}/{1}".format(cpath,'networkcontext_1.csv')

    connections = get_in_out_and_twoway_conns(expanded_search,top_results)
    inbound,outbound,twoway = add_network_context(nwloc,connections["inbound"] \
    ,connections["outbound"],connections["twoway"])

    inbound,outbound,twoway = add_geospatial_info(iploc,inbound,outbound,twoway)
    create_impact_analysis(ip, inbound,outbound,twoway, "",date)
    create_map_view(ip,inbound,outbound,twoway,date,iploc)
    create_incident_progression(ip,inbound,outbound,twoway,date)
    create_time_line(ip,inbound,outbound,twoway,date)
    save_comment(ip,title,text,date)
    return True

"""
--------------------------------------------------------------------------
Calculate number of inbound only, two-way, and outbound only.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def get_in_out_and_twoway_conns(expanded_search,top_results=20):

    inbound = {}
    outbound = {}
    twoway = {}
    srcdict = {}
    dstdict = {}
    conns_dict= {}
    rowct = 0

    for row in expanded_search:
        srcdict[row['srcIp']] = {
            'ip_int': struct.unpack("!L", socket.inet_aton(str(row['srcIp'])))[0],
            'dst_ip': row['dstIp'],
            'dst_ip_int': struct.unpack("!L", socket.inet_aton(str(row['dstIp'])))[0],
            'conns': int(row['connections']),
            'maxbytes': int(row['maxBytes'])
        }
        dstdict[row['dstIp']] = {
            'ip_int': struct.unpack("!L", socket.inet_aton(str(row['dstIp'])))[0],
            'src_ip': row['srcIp'],
            'src_ip_int': struct.unpack("!L", socket.inet_aton(str(row['srcIp'])))[0],
            'conns': int(row['connections']),
            'maxbytes': int(row['maxBytes'])
        }
        rowct +=1

    if rowct > 0:
        for result in srcdict:
            if result in dstdict:
                twoway[result] = srcdict[result]
            else:
                outbound[result] = srcdict[result]

        for result in dstdict:
            if result not in srcdict:
                inbound[result] = dstdict[result]

    top_inbound_b = {}
    top_outbound_b = {}
    top_twoway_b = {}

    if len(inbound) > 0:
        top_inbound_b = get_top_bytes(inbound,top_results)
        top_inbound_conns = get_top_conns(inbound,top_results)
        top_inbound_b.update(top_inbound_conns) # merge the two dictionaries
    if len(outbound) > 0:
        top_outbound_b = get_top_bytes(outbound,top_results)
        top_outbound_conns = get_top_conns(outbound,top_results)
        top_outbound_b.update(top_outbound_conns) # merge the two dictionaries
    if len(twoway) > 0:
        top_twoway_b = get_top_bytes(twoway,top_results)
        top_twoway_conns = get_top_conns(twoway,top_results)
        top_twoway_b.update(top_twoway_conns) # merge the two dictionaries


    result = {}
    result["inbound"] = top_inbound_b
    result["outbound"] = top_outbound_b
    result["twoway"] = top_twoway_b

    return result

"""
--------------------------------------------------------------------------
Create incident progression file.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def create_incident_progression(anchor, inbound, outbound, twoway, date):

    dendro_fpath = 'threat-dendro-' + anchor.replace('.','_') + ".json"
    obj = {
        'name':anchor,
        'children': [],
        'time': ""
    }

    #----- Add Inbound Connections-------#
    if len(inbound) > 0:
        obj["children"].append({'name': 'Inbound Only', 'children': [], 'impact': 0})
        in_ctxs = {}
        for ip in inbound:
            if 'nwloc' in inbound[ip] and len(inbound[ip]['nwloc']) > 0:
                ctx = inbound[ip]['nwloc'][2]
                if ctx not in in_ctxs:
                    in_ctxs[ctx] = 1
                else:
                    in_ctxs[ctx] += 1
        for ctx in in_ctxs:
            obj["children"][0]['children'].append({
                    'name': ctx,
                    'impact': in_ctxs[ctx]
                })

    #------ Add Outbound ----------------#
    if len(outbound) > 0:
        obj["children"].append({'name':'Outbound Only','children':[],'impact':0})
        out_ctxs = {}
        for ip in outbound:
            if 'nwloc' in outbound[ip] and len(outbound[ip]['nwloc']) > 0:
                ctx = outbound[ip]['nwloc'][2]
                if ctx not in out_ctxs:
                    out_ctxs[ctx] = 1
                else:
                    out_ctxs[ctx] += 1
        for ctx in out_ctxs:
            obj["children"][1]['children'].append({
                    'name': ctx,
                    'impact': out_ctxs[ctx]
                })

    #------ Add TwoWay ----------------#
    if len(twoway) > 0:
        obj["children"].append({'name':'two way','children': [], 'impact': 0})
        tw_ctxs = {}
        for ip in twoway:
            if 'nwloc' in twoway[ip] and len(twoway[ip]['nwloc']) > 0:
                ctx = twoway[ip]['nwloc'][2]
                if ctx not in tw_ctxs:
                    tw_ctxs[ctx] = 1
                else:
                    tw_ctxs[ctx] += 1

        for ctx in tw_ctxs:
            obj["children"][2]['children'].append({
                    'name': ctx,
                    'impact': tw_ctxs[ctx]
                })

    app_path = Configuration.spot()
    hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
    .format(app_path,date.year,date.month,date.day,anchor.replace(".","_"))

    if HDFSClient.put_file_json(obj,hdfs_path,dendro_fpath,overwrite_file=True):
        return "Incident progression successfully created \n"
    else:
        return "Incident progression couldn't be created \n"

"""
--------------------------------------------------------------------------
Create map view for StoryBoard.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def create_map_view(ip, inbound, outbound, twoway,date,iploc):

    iplist = ''
    globe_fpath = 'globe-' + ip.replace('.','_') + ".json"
    if os.path.isfile(iploc):
        iplist = np.loadtxt(iploc,dtype=np.uint32,delimiter=',',usecols={0},\
        converters={0: lambda s: np.uint32(s.replace('"',''))})
    else:
        print "No iploc.csv file was found, Map View map won't be created"

    response = ""
    if iplist != '':
        
        globe_json = {}
        globe_json['type'] = "FeatureCollection"
        globe_json['sourceips'] = []
        globe_json['destips'] = []
        for srcip in twoway:
            try:
                row =  twoway[srcip]['geo']
                globe_json['destips'].append({
                        'type': 'Feature',
                        'properties': {
                            'location':row[8],
                            'ip':srcip,
                            'type':1
                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [float(row[7]), float(row[6])]
                        }
                    })
            except ValueError:
                pass
        for dstip in outbound:
            try:
                row =  outbound[dstip]['geo']
                dst_geo = outbound[dstip]['geo_dst']
                globe_json['sourceips'].append({
                        'type': 'Feature',
                        'properties': {
                            'location':row[8],
                            'ip':dstip,
                            'type':3
                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [float(row[7]), float(row[6])]
                        }
                    })
                globe_json['destips'].append({
                        'type': 'Feature',
                        'properties': {
                            'location':row[8],
                            'ip':outbound[dstip]['dst_ip'],
                            'type':3
                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [float(dst_geo[7]), float(dst_geo[6])]
                        }
                    })
            except ValueError:
                pass
        for dstip in inbound:
            try:
                row =  inbound[dstip]['geo']
                dst_geo = inbound[dstip]['geo_src']
                globe_json['sourceips'].append({
                        'type': 'Feature',
                        'properties': {
                            'location':row[8],
                            'ip':dstip,
                            'type':2
                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [float(row[7]), float(row[6])]
                        }
                    })
                globe_json['destips'].append({
                        'type': 'Feature',
                        'properties': {
                            'location':row[8],
                            'ip':inbound[dstip]['src_ip'],
                            'type':2
                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [float(dst_geo[7]), float(dst_geo[6])]
                        }
                    })
            except ValueError:
                pass
        json_str = json.dumps(globe_json)
        app_path = Configuration.spot()
        hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
        .format(app_path,date.year,date.month,date.day,ip.replace(".","_"))

        if HDFSClient.put_file_json(globe_json,hdfs_path,globe_fpath,overwrite_file=True) :
            response = "Geolocation map successfully created \n"
        else:
            response = "The map can't be created without an iploc file \n"

    return response

"""
--------------------------------------------------------------------------
Create timeline for storyboard.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def create_time_line(anchor,inbound, outbound, twoway,date):

    top_keys = []
    if len(twoway) > 0: top_keys.extend(twoway.keys())
    if len(outbound) > 0: top_keys.extend(outbound.keys())
    if len(inbound) > 0: top_keys.extend(inbound.keys())


    db = Configuration.db()

    imp_query =("""
        INSERT INTO TABLE {0}.flow_timeline PARTITION (y={4}, m={5},d={6})
        SELECT
            '{7}' ,min(treceived) as tstart, max(treceived) as tend,
            sip as srcIP,dip as dstip, proto as proto, sport as sport,
            dport AS dport, ipkt as ipkt, ibyt as ibyt
        FROM
            {0}.flow
        WHERE y={4} AND m={5} AND d={6}
        AND ((dip IN({1}) AND sip ='{2}') OR (sip IN({1}) AND dip ='{2}'))
        GROUP BY sip, dip, proto, sport, dport, ipkt, ibyt
        ORDER BY tstart
        LIMIT {3}
    """)

    ips = "'" + "','".join(top_keys) + "'"
    imp_query = imp_query.format(db,ips,anchor,1000,date.year,date.month, date.day,anchor)

    if ImpalaEngine.execute_query(imp_query):
        return "Timeline successfully created \n"
    else:
        return "Timeline couldn't be created \n"

"""
--------------------------------------------------------------------------
Create Impact Analysis for StoryBoard.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def create_impact_analysis(anchor, inbound, outbound, twoway, threat_name,date):

    stats_fpath = 'stats-' + anchor.replace('.','_') + ".json"

    obj = {
        'name':threat_name,
        'children': [],
        'size': len(inbound) + len(outbound) + len(twoway)
    }

    #----- Add Inbound Connections-------#
    obj["children"].append({'name': 'Inbound Only', 'children': [], 'size': len(inbound)})
    in_ctxs = {}
    for ip in inbound:
        full_ctx = ''
        if 'nwloc' in inbound[ip] and len(inbound[ip]['nwloc']) > 0:
            full_ctx = inbound[ip]['nwloc'][2].split('.')[0]
        ctx = get_ctx_name(full_ctx)
        if ctx not in in_ctxs:
            in_ctxs[ctx] = 1
        else:
            in_ctxs[ctx] += 1
    for ctx in in_ctxs:
        obj["children"][0]['children'].append({
                'name': ctx,
                'size': in_ctxs[ctx]
            })


    #------ Add Outbound ----------------#
    obj["children"].append({'name':'Outbound Only','children':[],'size':len(outbound)})
    out_ctxs = {}
    for ip in outbound:
        full_ctx = ''
        if 'nwloc' in outbound[ip] and len(outbound[ip]['nwloc']) > 0:
            full_ctx = outbound[ip]['nwloc'][2].split('.')[0]
        ctx = get_ctx_name(full_ctx)
        if ctx not in out_ctxs:
            out_ctxs[ctx] = 1
        else:
            out_ctxs[ctx] += 1
    for ctx in out_ctxs:
        obj["children"][1]['children'].append({
                'name': ctx,
                'size': out_ctxs[ctx]
            })

    #------ Add Twoway ----------------#
    obj["children"].append({'name': 'two way', 'children': [], 'size': len(twoway)})
    tw_ctxs = {}
    for ip in twoway:
        full_ctx = ''
        if 'nwloc' in twoway[ip] and len(twoway[ip]['nwloc']) > 0:
            full_ctx = twoway[ip]['nwloc'][2].split('.')[0]
        ctx = get_ctx_name(full_ctx)
        if ctx not in tw_ctxs:
            tw_ctxs[ctx] = 1
        else:
            tw_ctxs[ctx] += 1

    for ctx in tw_ctxs:
        obj["children"][2]['children'].append({
                'name': ctx,
                'size': tw_ctxs[ctx]
            })

    app_path = Configuration.spot()
    hdfs_path = "{0}/flow/oa/storyboard/{1}/{2}/{3}/{4}" \
    .format(app_path,date.year,date.month,date.day,anchor.replace(".","_"))

    data = json.dumps(obj)
    if HDFSClient.put_file_json(obj,hdfs_path,stats_fpath,overwrite_file=True):
        return "Stats file successfully created \n"
    else:
        return "Stats file couldn't be created \n"

"""
--------------------------------------------------------------------------
Get topbytess list.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def get_top_bytes(conns_dict, top):
    topbytes = sorted(conns_dict.iteritems(),key=lambda (x,y): y['maxbytes'],reverse=True)
    topbytes = topbytes[0:top]
    return dict(topbytes)

"""
--------------------------------------------------------------------------
Get top connections.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def get_top_conns(conns_dict, top):
    topconns = sorted(conns_dict.iteritems(), key=lambda (x,y): y['conns'], reverse=True)
    topconns = topconns[0:top]
    return dict(topconns)

"""
--------------------------------------------------------------------------
Get network context - get start and end ranges.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def add_network_context(nwloc,inbound,outbound,twoway):
    nwdict = {}
    if os.path.isfile(nwloc) :
        with open(nwloc, 'r') as f:
            reader = csv.reader(f,delimiter=',')
            reader.next()
            #address range, description
            for row in reader:

                if '/' in row[0]:
                    #Range in subnet
                    iprange = row[0].split('/')
                    if len(iprange) < 2:
                        ipend = 0
                    else:
                        ipend = int(iprange[1])
                    nwdict[row[0]] = [struct.unpack("!L", \
                    socket.inet_aton(iprange[0]))[0],\
                    struct.unpack("!L",socket.inet_aton(iprange[0]))[0]+2**(32-ipend)-1, row[1]]
                elif '-' in row[0]:
                    #IP Range
                    iprange = row[0].split('-')
                    nwdict[row[0]] = [struct.unpack("!L",\
                    socket.inet_aton(iprange[0].replace(" ", "")))[0],\
                    struct.unpack("!L", socket.inet_aton(iprange[1].replace(" ", "")))[0], row[1]]
                else:
                    #Exact match
                    nwdict[row[0]] = [struct.unpack("!L",\
                    socket.inet_aton(row[0]))[0],struct.unpack("!L",\
                    socket.inet_aton(row[0]))[0], row[1]]

        for srcip in outbound:
            temp_ip = struct.unpack("!L", socket.inet_aton(srcip))[0]
            if srcip in nwdict:
                inbound[srcip]['nwloc'] = nwdict[srcip]
            else:
                matchingVals = [x for x in nwdict if nwdict[x][1] >= temp_ip and nwdict[x][0] <= temp_ip]
                outbound[srcip]['nwloc'] = nwdict[matchingVals[0]] if len(matchingVals) > 0 else ''

        for dstip in twoway:
            temp_ip = struct.unpack("!L", socket.inet_aton(dstip))[0]
            if dstip in nwdict:
                twoway[dstip]['nwloc'] = nwdict[dstip]
            else:
                matchingVals = [x for x in nwdict if nwdict[x][1] >= temp_ip and nwdict[x][0] <= temp_ip]
                twoway[dstip]['nwloc'] = nwdict[matchingVals[0]] if len(matchingVals) > 0 else ''

        for srcip in inbound:
            temp_ip = struct.unpack("!L", socket.inet_aton(srcip))[0]
            if srcip in nwdict:
                inbound[srcip]['nwloc'] = nwdict[srcip]
            else:
                matchingVals = [x for x in nwdict if nwdict[x][1] >= temp_ip and nwdict[x][0] <= temp_ip]
                inbound[srcip]['nwloc'] = nwdict[matchingVals[0]] if len(matchingVals) > 0 else ''

    return inbound,outbound,twoway


"""
--------------------------------------------------------------------------
Add Geo spatial info
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def add_geospatial_info(iploc,inbound,outbound,twoway):
    iplist = ''
    if os.path.isfile(iploc):
        iplist = np.loadtxt(iploc,dtype=np.uint32,delimiter=',',usecols={0},\
        converters={0: lambda s: np.uint32(s.replace('"',''))})
    else:
        print "No iploc.csv file was found, Map View map won't be created"


    # get geospatial info, only when iplocation file is available
    if iplist != '':
        for srcip in outbound:
            reader = csv.reader([linecache.getline(\
            iploc, bisect.bisect(iplist,outbound[srcip]['ip_int'])).replace('\n','')])

            outbound[srcip]['geo'] = reader.next()
            reader = csv.reader([linecache.getline(\
            iploc, bisect.bisect(iplist,outbound[srcip]['dst_ip_int'])).replace('\n','')])
            outbound[srcip]['geo_dst'] = reader.next()

        for dstip in twoway:
            reader = csv.reader([linecache.getline(\
            iploc,bisect.bisect(iplist,twoway[dstip]['ip_int'])).replace('\n','')])
            twoway[dstip]['geo'] = reader.next()

        for srcip in inbound:
            reader = csv.reader([linecache.getline(\
            iploc, bisect.bisect(iplist,inbound[srcip]['ip_int'])).replace('\n','')])

            inbound[srcip]['geo'] = reader.next()
            reader = csv.reader([linecache.getline(\
            iploc, bisect.bisect(iplist,inbound[srcip]['src_ip_int'])).replace('\n','')])
            inbound[srcip]['geo_src'] = reader.next()

    return inbound,outbound,twoway

"""
--------------------------------------------------------------------------
Get context name.
Migrated from IPython NoteBooks.
--------------------------------------------------------------------------
"""
def get_ctx_name(full_context):
    ctx= 'DMZ'
    if "VPN" in full_context:
        ctx = "VPN"
    elif "DMZ" in full_context:
        ctx = "DMZ"
    elif "Proxy" in full_context:
        ctx = "Proxy"
    elif "FW" in full_context:
        ctx = "FW"
    return ctx

"""
--------------------------------------------------------------------------
Reset scored connections.
--------------------------------------------------------------------------
"""
def reset_scored_connections(date):

    flow_storyboard =  "flow/hive/oa/storyboard"
    flow_threat_investigation = "flow/hive/oa/threat_investigation"
    flow_timeline = "flow/hive/oa/timeline"    
    app_path = Configuration.spot()   

    try:
        # remove parquet files manually to allow the comments update.
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,flow_storyboard,date.year,date.month,date.day) , "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,flow_threat_investigation,date.year,date.month,date.day), "impala")
        HDFSClient.delete_folder("{0}/{1}/y={2}/m={3}/d={4}/".format( \
            app_path,flow_timeline,date.year,date.month,date.day), "impala")
        ImpalaEngine.execute_query("invalidate metadata")
        return True
        
    except HdfsError:
        return False
