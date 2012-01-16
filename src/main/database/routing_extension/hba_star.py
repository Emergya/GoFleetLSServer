#!usr/bin/python
# Filename: hba_star.py

version = '0.1'

#In fact we don't need this
#But in case you try to execute this module outside postgres, you will know why you can't do it
import plpy

heuristic_plan = -1
adj_plan_rev = -1
adj_plan = -1
heuristic_constant=2
central_node_plan = -1
distance_plan = -1
distance_buffer = 200

#The heuristic used by A* is the euclidean length
def hba_heuristic(source, target, tablename='vertex', col_geom='geom', col_id='id'):
  if heuristic_plan == -1:
    global heuristic_plan
    heuristic_plan = plpy.prepare('select st_distance(a.' + col_geom + ', b.' + col_geom + ') as cost from ' + tablename + ' as a, ' + tablename + ' as b where a.' + col_id + ' = $2 and b.' + col_id + ' = $3', ['text', 'integer', 'integer', 'text', 'text'])
  try:
    return plpy.execute(heuristic_plan, [col_geom, source, target, tablename, col_id], 1)[0]['cost']
  except:
    plpy.info("No heuristic distance. This is a bug, probably.")
    return float('inf')

#Final procedure
#Once we have the two partial paths, we build the definitive path
def hba_buildPath(ps, pt, m, source, target, olf, olb):
  #Return variable
  res = []

  current = m

  if m ==  0:
    m = hba_bestNext(olf)
  if m == -1:
    m = hba_bestNext(olb)

  plpy.info(m)

  try:
    while current != source:
#     plpy.info("Current-s: ", ps[current][0])
     current = int(ps[current][0])
#     res.append([int(ps[current][1]['id']), ps[current][1]['geom'], "name", ps[current][1]['cost']])
     res.append([int(ps[current][1]['id']), ps[current][1]['geom'], ps[current][1]['name'], ps[current][1]['cost']])
  except:
    pass

  res.reverse()

  current=m

  try:
    while current != target:
     current = int(pt[current][0])
#     res.append([int(pt[current][1]['id']), pt[current][1]['geom'], "name", pt[current][1]['cost']])
     res.append([int(pt[current][1]['id']), pt[current][1]['geom'], pt[current][1]['name'], pt[current][1]['cost']])
  except:
    pass

  plpy.info("Path: ", len(res)) #, res)

  return res

#The step cost is the cost of the edge
def hba_stepcost(row):
  if row is None:
    return float('inf')  
  return row['length']

#Candidates to be the next node
#The function returns edges
def hba_adj(cat, source, target, p, tablename='routing', col_geom='geom', col_edge='id', col_cost='cost', col_source='source', col_target='target', col_revc='reverse_cost', col_cat='category', col_name='name', col_rule='rule'):

  name = ''

  try:
    last_id = int(p[int(source)][1]['id'])
    name = p[int(source)][1]['name']
  except:
    last_id = -1
 
  if "source" in col_source:
    return plpy.execute(adj_plan, [source, last_id, target, name, cat[0]])
  else:
    return plpy.execute(adj_plan_rev, [source, last_id, target, name, cat[0]])

def hba_bestNext(ol):
  cost_tmp = float('inf')
  x = -1
#  x <- node with smallest f-value
  for k,v in ol.items():
    if cost_tmp > v:
      x = k
  return x

def hba_process_y(adj, p, cat, d, ol, cl, x, target, vertex_tablename, col_vertex_geom, col_edge, already_processed=[], distance='Infinity'):
  cat[0] = cat[0] + 1
  categories = [hba_process_vertex(y, p, cat, d, ol, cl, x, target, vertex_tablename, col_vertex_geom, col_edge, already_processed, distance) for y in adj]
  if len(already_processed) == 0 and len(adj) > 0  and cat[0] <= 4:
   cat[0] = cat[0] + 2 
   hba_process_y(adj, p, cat, d, ol, cl, x, target, vertex_tablename, col_vertex_geom, col_edge, already_processed, distance)

def hba_process_vertex(y, p, cat_array, d, ol, cl, x, target, vertex_tablename, col_vertex_geom, col_edge, already_processed, distance):
  cat = cat_array[0]
  #Maybe we have reached this vertex before, on a better way
  global distance_buffer
  if hba_check_if_already_reached(y, already_processed, cat, distance <= distance_buffer):
    already_processed.append(y['id'])
    #Link is of current category or better
    cost = d[x] + hba_stepcost(y)
    y_id = y['target']
    if hba_check_if_better(y_id, ol, cl, cost, d):
      cat_array[0] = hba_update_y(x, y, y_id, d, p, cat, cl, ol, cost, target, vertex_tablename, col_vertex_geom, col_edge)

def hba_check_if_already_reached(y, already_processed, cat, local=0):
  return (not local or y['category'] <= cat) and not y['id'] in already_processed

def hba_check_if_better(y_id, ol, cl, cost, d):
  return (not(y_id in ol) and not(y_id in cl)) or cost < d[y_id]

def hba_update_y(x, y, y_id, d, p, cat, cl, ol, cost, target, vertex_tablename, col_vertex_geom, col_edge):
  #Update y
  d[y_id] = cost
  p[y_id] = [x, y]
  if (y_id in cl):
    cl.remove(y_id)
  ol[y_id] = cost + y['heuristic'] #hba_heuristic(y_id, target, vertex_tablename, col_vertex_geom, col_edge)
  return min(cat, y['category'])

#A* function, but just one step
def hba_astar(source, target, ol, cl, cl2, cat, d, p, tablename='routing', col_geom='geom', col_edge='id', col_cost='cost', col_revc='reverse_cost', col_source='source', col_target='target', vertex_tablename='vertex', col_cat='category', col_vertex_geom='geom', col_name='name', col_rule='rule'):
  #If we don't have open candidates...
  if len(ol) == 0:
    return 0
 
  if len(ol) > 0:
    #x <- node with smallest f-value
    x = hba_bestNext(ol)

    #We move through the next best option:
    cl.append(x)
    del ol[x]

    #Have we just found the middle point?
    if (x == target or x in cl2):
      try:
        last_id = int(p[x][1]['id'])
      except:
        last_id = -1
      global central_node_plan

      if "source" in col_source:
        check_x = plpy.execute(central_node_plan, [x, last_id])
      else: 
        check_x = plpy.execute(central_node_plan, [last_id, x])

      for checking in check_x:
        return x

    #Next candidates
    # If we are in the initialization buffer, use hba_adj_initialization
    if distance_plan == -1:
        global distance_plan
        distance_plan = plpy.prepare('\n\
            ' + 'SELECT min(st_distance_sphere(v1.geom, v2.geom)) as dist from vertex v1, vertex v2 where v1.id = $1 and (v2.id = $2 or v2.id = $3)',['Integer', 'Integer', 'Integer'])
    distance =plpy.execute(distance_plan, [x, source, target], 1)[0]["dist"]
    adj = hba_adj(cat, x, target, p,tablename, col_geom, col_edge, col_cost, col_source, col_target, col_revc, col_cat, col_name, col_rule)

    #Forever alone
    if adj is None:
      plpy.error("This vertex is alone")

    #For each candidate
    hba_process_y(adj, p, cat, d, ol, cl, x, target, vertex_tablename, col_vertex_geom, col_edge, [],  distance)
  
  #Return false, we still have to loop more
  return 0

#Public function
#Implements HBA* algorithm
def hba_star_pl(source, target, tablename='routing', col_edge='id', col_cost='cost', col_revc='reverse_cost', col_rule='rule', col_source='source', col_target='target', col_geom='the_geom', col_name='name', col_cat='category', vertex_tablename='vertex', col_vertex_geom='geom'):
  
  #Closed Lists (backward and forward)
  clf = []
  clb = []

  #Open Lists (backward and forward)
  #Candidate nodes from which we want to continue calculating
  #Every node (key) contains (value) its estimated (heuristic) cost till the target
  olf = {}
  olb = {}
  
  #Total cost (backward and forward)
  #For every node, globally, its lowest cost from source/target
  d = {}
  d[target] = 0
  d[source] = 0

  #Predecessor array (backward and forward)
  #For every node, which is its previous node (related to d[])
  ps = {}
  pt = {}

  #Current category of search (backward and forward)
  catf = [10]
  catb = [10]

  #Initial values
  olf[source] = d[source] + hba_heuristic(source, target, vertex_tablename, col_vertex_geom, col_edge)
  olb[target] = d[target] + hba_heuristic(target, source, vertex_tablename, col_vertex_geom, col_edge)

  global adj_plan
  global adj_plan_rev
  global central_node_plan
  
  adj_plan_rev = plpy.prepare('select a.*, '
	+ 'CASE WHEN a.name = $4 AND a.category <= 4 THEN \n\
		(a.length + st_distance_sphere(st_startpoint(a.geom), b.geom)) * (a.category + 1) \n\
	WHEN a.name = b.name THEN \n\
                a.length + st_distance_sphere(st_startpoint(a.geom), b.geom) \n\
	ELSE \n\
		(a.length + st_distance_sphere(st_startpoint(a.geom), b.geom)) * ' + str(heuristic_constant) + ' * (a.category + 1) \n\
	END as heuristic \n\
	from ((select  \n\
        ' + col_geom + ' as geom, \n\
        ' + col_edge + ' as id, \n\
        ' + col_cost + ' as cost,\n\
        st_distance_sphere(st_startpoint(' + col_geom + '), st_endpoint(' + col_geom + ')) as length,\n\
        ' + col_target + ' as source, \n\
        ' + col_source + ' as target, \n\
        ' + col_cat + ' as category, \n\
        ' + col_name + ' as name from ' + tablename + '\n\
        ) union all (select  \n\
        st_reverse(' + col_geom + ') as geom, \n\
        ' + col_edge + ' as id, \n\
        ' + col_revc + ' as cost, \n\
        st_distance_sphere(st_startpoint(' + col_geom + '), st_endpoint(' + col_geom + ')) as length,\n\
        ' + col_source + ' as source,\n\
        ' + col_target + ' as target, \n\
        ' + col_cat + ' as category, \n\
        ' + col_name + ' as name from ' + tablename + ' )\n\
        ) a, (select st_startpoint(' + col_geom + ')  as geom from ' + tablename + ' where ' + col_source + ' = $3 or ' + col_target + ' = $3 limit 1) b \n\
                where source = $1 and category >= 0\n\
                and cost <> \'Infinity\''
             #Turn restrictions:
        + 'and ' + col_edge + ' not in (SELECT ' + col_edge + ' from ' + tablename + ' r where r.' + col_rule + ' = $2)'
       , ['Integer', 'Integer', 'Integer', 'text', 'Integer'])

  adj_plan = plpy.prepare('\n\
    select a.*,'
        + 'CASE WHEN a.name = $4 AND a.category <= 4 THEN \n\
                (a.length + st_distance_sphere(st_startpoint(a.geom), b.geom))  * (a.category + 1) \n\
        WHEN a.name = b.name THEN \n\
                a.length + st_distance_sphere(st_startpoint(a.geom), b.geom) \n\
        ELSE \n\
                (a.length + st_distance_sphere(st_startpoint(a.geom), b.geom)) * ' + str(heuristic_constant) + ' * (a.category + 1) \n\
        END as heuristic \n\
        from ((select  \n\
	st_reverse(' + col_geom + ') as geom, \n\
	' + col_edge + ' as id, \n\
	' + col_revc + ' as cost,\n\
        st_distance_sphere(st_startpoint(' + col_geom + '), st_endpoint(' + col_geom + ')) as length,\n\
	' + col_target + ' as source, \n\
	' + col_source + ' as target, \n\
	' + col_cat + ' as category, \n\
	' + col_name + ' as name from ' + tablename + '\n\
	) union all (select  \n\
	' + col_geom + ' as geom, \n\
	' + col_edge + ' as id, \n\
	' + col_cost + ' as cost, \n\
        st_distance_sphere(st_startpoint(' + col_geom + '), st_endpoint(' + col_geom + ')) as length,\n\
	' + col_source + ' as source,\n\
	' + col_target + ' as target, \n\
	' + col_cat + ' as category, \n\
	' + col_name + ' as name from ' + tablename + ' )\n\
	) a, (select st_startpoint(' + col_geom + ')  as geom  from ' + tablename + ' where ' + col_source + ' = $3 or ' + col_target + ' = $3 limit 1) b \n\
		where source = $1 and category >= 0\n\
		and cost <> \'Infinity\''
             #Turn restrictions:
	+ 'and ' + col_edge + ' not in (SELECT ' + col_rule + ' from ' + tablename + ' r where r.' + col_edge + ' = $2 and ' + col_rule + ' is not null)'
       , ['Integer', 'Integer', 'Integer', 'text', 'Integer'])
 
  central_node_plan = plpy.prepare('SELECT 1 WHERE NOT EXISTS(SELECT 1 FROM ' + tablename + ' WHERE ' + col_edge + ' = $2 and ' + col_rule + ' = $1)', ['Integer', 'Integer'])

  #Star two-sided A* search

  m = -1
  m1 = 0
  m2 = 0

  #We try A* step by step until the two paths collided
  while not m1 and not m2 and (len(olf) > 0 and len(olb) > 0):
    m1 = hba_astar(source, target, olf, clf, clb, catf, d, ps, tablename, col_geom, col_edge, col_cost, col_revc, col_source, col_target, vertex_tablename, col_cat, col_vertex_geom, col_name, col_rule)
    m2 = hba_astar(target, source, olb, clb, clf, catb, d, pt, tablename, col_geom, col_edge, col_revc, col_cost, col_target, col_source, vertex_tablename, col_cat, col_vertex_geom, col_name, col_rule)

  m = m1
  if m <= 0 :
    m = m2

  plpy.info("cl:", len(clf) + len(clb))

  if m == 0:
    plpy.info("No path found. Inferring path")

  #Now, get the result
  return hba_buildPath(ps, pt, m, source, target, olf, olb)


# End of hba_star.py
