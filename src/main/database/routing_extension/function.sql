DROP TYPE IF EXISTS hba_res;
CREATE TYPE hba_res AS (id bigint, geom geometry, name text, cost double precision);

CREATE OR REPLACE FUNCTION hba(origin Geometry, goal Geometry)
  RETURNS SETOF hba_res AS
$BODY$
DECLARE
  rec RECORD;
  res hba_res%rowtype;
  s integer;
  t integer;
BEGIN
  select source into s from routing where cost <> 'Infinity' or reverse_cost <> 'Infinity' order by st_distance(the_geom, origin) asc limit 1;
  select target into t from routing where cost <> 'Infinity' or reverse_cost <> 'Infinity' order by st_distance(the_geom, goal) asc limit 1;
  for rec in select * from hba_(s,t) LOOP
        res.id := rec.hba_[1];
        res.geom := rec.hba_[2];
        res.name := rec.hba_[3];
        res.cost := rec.hba_[4];
        RETURN NEXT res;
  END LOOP;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 500
  ROWS 200;


CREATE OR REPLACE FUNCTION hba(source integer, target integer)
  RETURNS SETOF hba_res AS
$BODY$
DECLARE
  rec RECORD;
  res hba_res%rowtype;
BEGIN
  for rec in select * from hba_(source,target) LOOP
	res.id := rec.hba_[1];
	res.geom := rec.hba_[2];
	res.name := rec.hba_[3];
	res.cost := rec.hba_[4];
	RETURN NEXT res;
  END LOOP;
  RETURN;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 200;


-- Function: hba_(integer, integer)

-- DROP FUNCTION hba_(integer, integer);

CREATE OR REPLACE FUNCTION hba_(source integer, target integer)
  RETURNS SETOF text[] AS
$BODY$
  plpy.info("hba_star(", source, target, ")")

  import sys
  sys.path.append('/usr/share/gofleetls')
  import hba_star
  
  res = hba_star.hba_star(source,target);

  return ["{" + str(r[0]) + "," + str(r[1]) + "," + str(r[2]) + "," + str(r[3]) + "}" for r in res]
  
$BODY$
  LANGUAGE plpythonu VOLATILE
  COST 1000
  ROWS 200;


-- Function: gls_tsp(text, integer[], text, integer)
DROP FUNCTION gls_tsp(text, integer[], text, integer);

CREATE OR REPLACE FUNCTION gls_tsp(routingtable text, stoptable integer[], gid text, source integer)
  RETURNS SETOF hba_res AS
$BODY$
  routingRes = []
  source_ = source
  
  import sys
  sys.path.insert(1, '/usr/share/gofleetls/')
  import hba_star
  
  next_target_plan = plpy.prepare('SELECT t.target FROM ' + routingtable + ' s, ' 
				+ '(select * from ((select id, routing.source as target, ' 
				+ 'the_geom from ' + routingtable + ' as routing ' 
				+ 'where routing.source = ANY($1::INT[]) ' 
				+ 'and not routing.id = ANY($2::INT[])) ' 
				+ 'union all (select id, routing.target, ' 
				+ 'routing.the_geom from ' + routingtable + ' as routing  ' 
				+ 'where routing.target = ANY($1::INT[]) ' 
				+ 'and not routing.id = ANY($2::INT[]))) t_) t ' 
				+ 'WHERE (s.source = $3 or s.target = $3) and $3 <> t.target ' 
				+ 'order by st_distance(t.the_geom, s.the_geom) asc', 
				['int[]', 'int[]', 'Integer'])

  while not set(stoptable).issubset(set(routingRes)):
    target = plpy.execute(next_target_plan, [stoptable, routingRes, source_], 1)
    for target_ in target:
      haspath = 0
      
      plpy.info(str(source_) + "=>" +  str(target_['target']))

      res = hba_star.hba_star_pl(source_, target_['target'])

      plpy.info("done")
      
      for r in res:
        yield(str(r[0]), str(r[1]), str(r[2]), str(r[3]))
        haspath = 1
        
      if haspath:
        source_ = target_['target']
      try:
	stoptable.remove(target_['target'])
      except:
        plpy.info("Probable bug, we tried to remove " + str(target_['target']) + " from the stoptable")
    
$BODY$
  LANGUAGE plpythonu IMMUTABLE
  COST 5000
  ROWS 200;

