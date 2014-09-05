CREATE OR REPLACE FUNCTION hba_(source integer, target integer)
  RETURNS SETOF hba_res AS
$BODY$
  plpy.info("hba_star(", source, target, ")")
  import sys
  sys.path.insert(1, '/usr/share/gofleetls/')
  import hba_star
  res = hba_star.hba_star_pl(source,target)

  for r in res:
    yield(str(r[0]), str(r[1]), str(r[2]), str(r[3]))
  
$BODY$
  LANGUAGE plpythonu VOLATILE
  COST 2000
  ROWS 200;

CREATE OR REPLACE FUNCTION hba(source integer, target integer)
  RETURNS SETOF hba_res AS
$BODY$
DECLARE
  rec RECORD;
  res hba_res%rowtype;
BEGIN
  RETURN query select * from hba_(source,target);
END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 2000
  ROWS 200;

CREATE OR REPLACE FUNCTION hba(origin geometry, goal geometry)
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
  RETURN QUERY select * from hba(source, target);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 2000
  ROWS 200;

CREATE OR REPLACE FUNCTION gls_tsp(routingtable text, stoptable integer[], gid text, source integer)
  RETURNS SETOF hba_res AS
$BODY$
DECLARE
        target INTEGER;
        source_ INTEGER;
        haspath BOOLEAN := false;
        r RECORD;
        routingRes INTEGER[]:='{}';
        BEGIN
                RAISE INFO 'gls_tsp(%, %, %, %)', routingTable, stopTable, gid, source;
                source_ := source;
                WHILE NOT(stopTable <@ routingRes) LOOP
                       -- RAISE INFO 'Looping on %, %', stopTable, routingRes;
                        -- we put in target the next nearest element --
                        SELECT t.target INTO target FROM routing s, 
				(select * from ((select id, routing.source as target, the_geom from routing 
							where routing.source = ANY (stopTable)
							and not routing.id = ANY (routingres))
						union (select id, routing.target, routing.the_geom from routing
							where routing.target = ANY (stopTable)
							and not routing.id = ANY (routingres))
						) t_) t
                                WHERE (s.source = source_ or s.target = source_)
                                and source_ <> t.target
                                order by st_distance(t.the_geom, s.the_geom) asc
                                limit 1;

                        FOR r in SELECT * from hba(source_, target) LOOP
                                routingRes := routingRes || r.id;
                                haspath := true;
                                RETURN NEXT r;
                        END LOOP;
                        IF haspath THEN
				SELECT target INTO source_;
                        END IF;
                        SELECT ARRAY(SELECT * FROM (SELECT UNNEST(stoptable) as a) t WHERE t.a <> target) INTO stopTable;
                        haspath := false;
                END LOOP;
        END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 2000
  ROWS 100;


CREATE OR REPLACE FUNCTION gls_tsp(routingtable text, stoptable_g geometry[], gid text, source_g geometry)
  RETURNS SETOF hba_res AS
$BODY$
DECLARE
        source_ INTEGER;
        t INTEGER := 1;
        i INTEGER := 1;
        stoptable INTEGER[]:='{}';
        r INTEGER;
        BEGIN
		select source into source_ from routing 
			where cost <> 'Infinity' 
			or reverse_cost <> 'Infinity' 
			order by st_distance(the_geom, source_g) asc limit 1;

		-- For each "i" between lower limit and upper limit of array: --
		FOR i IN array_lower(stoptable_g,1) .. array_upper(stoptable_g,1) LOOP
			select target into t from routing 
				where cost <> 'Infinity' 
				or reverse_cost <> 'Infinity' 
				order by st_distance(the_geom, stoptable_g[i]) asc limit 1;
			stopTable := stopTable || t;
		END LOOP;

		RAISE INFO 'select * from gls_tsp(%, %, %, %)', routingTable, stopTable, gid, source_;
		
                RETURN QUERY select * from gls_tsp(routingTable, stopTable, gid, source_);
        END;
$BODY$
  LANGUAGE plpgsql IMMUTABLE
  COST 2000
  ROWS 200;


