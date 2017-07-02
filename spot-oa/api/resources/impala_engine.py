from impala.dbapi import connect
import api.resources.configurator as Config

def create_connection():

    impala_host, impala_port =  Config.impala()
    db = Config.db()
    conn = connect(host=impala_host, port=int(impala_port),database=db)
    return conn.cursor()

def execute_query(query,fetch=False):

    impala_cursor = create_connection()
    impala_cursor.execute(query)

    return impala_cursor if not fetch else impala_cursor.fetchall()

def execute_query_as_list(query):

    query_results = execute_query(query)
    row_result = {}
    results = []

    for row in query_results:
        x=0
        for header in query_results.description:
            row_result[header[0]] = row[x]
            x +=1
        results.append(row_result)
        row_result = {}

    return results


