#!/bin/sh

#Configuration:
#Customize the following variables with your own environment:
DATABASE="gofleetls"
USERNAME="gofleetls"
HOST="localhost"
PORT="5433"
SHP_PATH="~/gofleetls/";


#Shouldn't touch the following code
HERE=$(pwd);
PSQL="psql -U $USERNAME $DATABASE -h $HOST -p $PORT"

cd $SHP_PATH;

echo "Loading NavTeq data"

for f in *.shp
do
        shp2pgsql -W Latin10 -s 4326 -I -d -k $f | $PSQL &
done;

for f in *.dbf
do
	shp2pgsql -W Latin10 -d -k $f | $PSQL
done;

cd $HERE

echo "Activating performance."

$PSQL -c "CREATE INDEX zlevels_combined_idx ON zlevels USING btree (link_id , point_num );"  &
$PSQL -c "CREATE INDEX zlevels_z_level_idx ON zlevels USING btree (z_level);" &

$PSQL -c "CREATE INDEX streets_linkid_idx ON streets USING btree (link_id);" &
$PSQL -c "CREATE INDEX streets_func_class_idx ON streets USING btree (func_class);" &
$PSQL -c "CREATE INDEX streets_st_name_idx ON streets USING btree (st_name);"&

$PSQL -c "CREATE INDEX cdms_linkid_idx ON cdms USING btree (link_id);"&
$PSQL -c "CREATE INDEX cdms_condid_idx ON cdms USING btree (cond_id);"&

$PSQL -c "CREATE INDEX rdms_linkid_idx ON rdms USING btree (cond_id);"&
$PSQL -c "CREATE INDEX rdms_condid_idx ON rdms USING btree (link_id);"&

$PSQL -c "VACUUM ANALYZE zlevels;" &
$PSQL -c "VACUUM ANALYZE rdms;"&
$PSQL -c "VACUUM ANALYZE cdms;"&
$PSQL -c "VACUUM ANALYZE streets;"

NOW=$(date +%s)

echo "Generating custom tables"

$PSQL -c "SELECT gls_createAllTables('$NOW'::VARCHAR);"
$PSQL -c "CREATE SEQUENCE id_value$NOW START 1;"

$PSQL -c "CREATE INDEX street_area_idx$NOW ON street_$NOW USING btree (st_area(geometry));"&
$PSQL -c "CREATE INDEX street_geometry_idx$NOW ON street_$NOW USING gist (geometry);"
$PSQL -c "ALTER TABLE street_$NOW CLUSTER ON street_geometry_idx$NOW;"&
$PSQL -c "CREATE INDEX street_id_idx$NOW ON street_$NOW USING btree (id);"&
$PSQL -c "CREATE INDEX street_name_idx$NOW ON street_$NOW USING btree (name);"&

$PSQL -c "CREATE INDEX municipality_subdivision_area_idx$NOW ON municipality_subdivision_$NOW USING btree (st_area(geometry));"&
$PSQL -c "CREATE INDEX municipality_subdivision_geometry_idx$NOW ON municipality_subdivision_$NOW USING gist (geometry);"
$PSQL -c "ALTER TABLE municipality_subdivision_$NOW CLUSTER ON municipality_subdivision_geometry_idx$NOW;"&
$PSQL -c "CREATE INDEX municipality_subdivision_id_idx$NOW ON municipality_subdivision_$NOW USING btree (id);"&
$PSQL -c "CREATE INDEX municipality_subdivision_name_idx$NOW ON municipality_subdivision_$NOW USING btree (name);"&

$PSQL -c "CREATE INDEX municipality_area_idx$NOW ON municipality_$NOW USING btree (st_area(geometry));"&
$PSQL -c "CREATE INDEX municipality_geometry_idx$NOW ON municipality_$NOW USING gist (geometry);"
$PSQL -c "ALTER TABLE municipality_$NOW CLUSTER ON municipality_geometry_idx$NOW;"&
$PSQL -c "CREATE INDEX municipality_id_idx$NOW ON municipality_$NOW USING btree (id);"&
$PSQL -c "CREATE INDEX municipality_name_idx$NOW ON municipality_$NOW USING btree (name);"&

$PSQL -c "CREATE INDEX country_subdivision_area_idx$NOW ON country_subdivision_$NOW USING btree (st_area(geometry));"&
$PSQL -c "CREATE INDEX country_subdivision_geometry_idx$NOW ON country_subdivision_$NOW USING gist (geometry);"
$PSQL -c "ALTER TABLE country_subdivision_$NOW CLUSTER ON country_subdivision_geometry_idx;"&
$PSQL -c "CREATE INDEX country_subdivision_id_idx$NOW ON country_subdivision_$NOW USING btree (id);"&
$PSQL -c "CREATE INDEX country_subdivision_name_idx$NOW ON country_subdivision_$NOW USING btree (name);"&

$PSQL -c "CREATE INDEX country_area_idx$NOW ON country_$NOW USING btree (st_area(geometry));"&
$PSQL -c "CREATE INDEX country_geometry_idx$NOW ON country_$NOW USING gist (geometry);"
$PSQL -c "ALTER TABLE country_$NOW CLUSTER ON country_geometry_idx$NOW;"&
$PSQL -c "CREATE INDEX country_id_idx$NOW ON country_$NOW USING btree (id);"&
$PSQL -c "CREATE INDEX country_name_idx$NOW ON country_$NOW USING btree (name);"&

$PSQL -c "INSERT INTO street_$NOW SELECT gid, CASE WHEN st_name IS NULL THEN '' ELSE st_name END, st_GeometryN(the_geom, 1) as geom FROM streets;" &
$PSQL -c "INSERT INTO municipality_subdivision_$NOW SELECT gid, CASE WHEN polygon_nm IS NULL THEN '' ELSE polygon_nm END, the_geom FROM AdminBndy5;"&
$PSQL -c "INSERT INTO country_subdivision_$NOW  SELECT gid, CASE WHEN polygon_nm IS NULL THEN '' ELSE polygon_nm END, the_geom FROM AdminBndy2;"&
$PSQL -c "INSERT INTO country_$NOW SELECT 1, 'SPA', ST_Union(the_geom) FROM AdminBndy2;"&
$PSQL -c "INSERT INTO municipality_$NOW SELECT nextval('id_value$NOW'), polygon_nm, the_geom FROM AdminBndy3;"&
$PSQL -c "INSERT INTO municipality_$NOW SELECT nextval('id_value$NOW'), polygon_nm, the_geom FROM AdminBndy4;"&

$PSQL -c "vacuum street_$NOW" &
$PSQL -c "vacuum municipality_subdivision_$NOW" &
$PSQL -c "vacuum country_subdivision_$NOW" &
$PSQL -c "vacuum country_$NOW" &
$PSQL -c "vacuum municipality_$NOW" &

echo "Converting data to PgRouting format"

$PSQL -c "DROP VIEW routing_restrictions_view CASCADE;"
$PSQL -c "DROP VIEW routing_norestrictions_view CASCADE;"

$PSQL -c "CREATE OR REPLACE VIEW routing_restrictions_view AS
SELECT 
        s.func_class::integer as category,
        s.link_id::integer as id,
        CASE s.dir_travel
                WHEN 'B' THEN st_length(s.the_geom)::double precision
                WHEN 'F' THEN st_length(s.the_geom)::double precision
                ELSE 'infinity'::double precision
        END as cost,
        CASE s.dir_travel
                WHEN 'B' THEN st_length(s.the_geom)::double precision
                WHEN 'T' THEN st_length(s.the_geom)::double precision
                ELSE 'infinity'::double precision
        END as reverse_cost,
        -1::integer as source,
        -1::integer as target,
        st_X(st_startpoint(s.the_geom::geometry))::double precision as x1,
        st_Y(st_startpoint(s.the_geom::geometry))::double precision as y1,
        st_X(st_endpoint(s.the_geom::geometry))::double precision as x2,
        st_Y(st_endpoint(s.the_geom::geometry))::double precision as y2,
        'infinity'::double precision as to_cost,
        man_linkid::integer as rule,
        s.the_geom as geometry,
	s.st_name as name
FROM 
        streets as s,
	cdms,
        rdms
WHERE 
	rdms.cond_id = cdms.cond_id
	AND s.link_id = cdms.link_id
	AND rdms.link_id = s.link_id
	AND (cdms.cond_type = 7 OR cdms.cond_type = 4);"

$PSQL -c "CREATE OR REPLACE VIEW routing_norestrictions_view AS
SELECT 
        s.func_class::integer as category,
        s.link_id::integer as id,
        CASE s.dir_travel
                WHEN 'B' THEN st_length(s.the_geom)::double precision
                WHEN 'F' THEN st_length(s.the_geom)::double precision
                ELSE 'infinity'::double precision
        END as cost,
        CASE s.dir_travel
                WHEN 'B' THEN st_length(s.the_geom)::double precision
                WHEN 'T' THEN st_length(s.the_geom)::double precision
                ELSE 'infinity'::double precision
        END as reverse_cost,
        -1::integer as source,
        -1::integer as target,
        st_X(st_startpoint(s.the_geom::geometry))::double precision as x1,
        st_Y(st_startpoint(s.the_geom::geometry))::double precision as y1,
        st_X(st_endpoint(s.the_geom::geometry))::double precision as x2,
        st_Y(st_endpoint(s.the_geom::geometry))::double precision as y2,
        'infinity'::double precision as to_cost,
        null::integer as rule,
        s.the_geom as geometry,
	s.st_name as name
FROM 
        streets as s;"

$PSQL -c "create or replace view routing_view as 
(select * from routing_restrictions_view) union all 
(select * from routing_norestrictions_view);"

echo "Generating first iteration of vials"

$PSQL -c "DROP TABLE routing_""$NOW;"

$PSQL -c "SELECT * INTO routing_""$NOW"" FROM routing_view;"

$PSQL -c "INSERT INTO geometry_columns (f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, type) values('', 'public', 'routing_""$NOW""', 'geometry', 2, 4326, 'LINESTRING');"

$PSQL -c "CREATE INDEX routing_""$NOW""_rule_idx ON routing_$NOW (rule ASC NULLS LAST);"&
$PSQL -c "CREATE INDEX routing_""$NOW""_id_idx ON routing_$NOW (id ASC NULLS LAST);"&
$PSQL -c "CREATE INDEX routing_""$NOW""_source_idx ON routing_$NOW (source ASC NULLS LAST);"&
$PSQL -c "CREATE INDEX routing_""$NOW""_target_idx ON routing_$NOW (target ASC NULLS LAST);"&

$PSQL -c "vacuum routing_$NOW"

$PSQL -c "SELECT 1 FROM assign_vertex_id('routing_""$NOW""', 0.00001, 'geometry', 'id');"

echo "Generating second iteration of vials (z-levels)"

$PSQL -c "DROP VIEW routing_zlevel_restrictions_view;"
$PSQL -c "CREATE OR REPLACE VIEW routing_zlevel_restrictions_view AS 
SELECT
        source.category::integer as category,
        source.id as id,
        source.cost as cost,
        source.reverse_cost as reverse_cost,
        source.source as source,
        source.target as target,
        source.x1 as x1,
        source.y1 as y1,
        source.x2 as x2,
        source.y2 as y2,
        'infinity'::double precision as to_cost,
        target.id::integer as rule,
        source.geometry as geometry,
	source.name as name
FROM 
        (SELECT r.*, z.z_level 
		FROM 
			routing_""$NOW"" r, 
			zlevels z 
		where 
			r.id = z.link_id 
		order by 
			z.point_num = 
				(select max(point_num) 
					from zlevels as l 
					where l.link_id = z.link_id)) as source, 
        (SELECT r.source, z.z_level, r.id 
		FROM 
			routing_""$NOW"" r, 
			zlevels z 
		where 
			r.id = z.link_id 
			and z.point_num = 
				(select max(point_num) 
					from zlevels as l 
					where l.link_id = z.link_id)) as target
WHERE source.target = target.source
AND source.z_level <> target.z_level;"

$PSQL -c "INSERT INTO routing_""$NOW"" SELECT * FROM routing_zlevel_restrictions_view;"

echo "Updating views"

$PSQL -c "DROP VIEW vertex;"

$PSQL -c "DROP VIEW routing;"

$PSQL -c "SELECT gls_updateAllTables('$NOW'::varchar);"

$PSQL -c "CREATE OR REPLACE VIEW vertex AS SELECT routing.geometry AS geom, routing.source AS id FROM routing;"

echo "Importation successfull."
