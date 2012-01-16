#!/bin/sh

echo "Script to transform TeleAtlas format to pgRouting format"
echo "Only works with routing data"
echo "It works only if the TeleAtlas data is already loaded on your database"
echo "and there is a 'routing' named table with the specific routing format"
echo "By executing this script you give us your soul and also accept that this script comes with no warranty"
echo "This means: You are the only one responsible of the damage caused by this script."

echo "Now, to important stuff:"

#Configuration:
#Customize the following variables with your own environment:
DATABASE="gofleetls-teleatlas"
USERNAME="gofleetls"
HOST="localhost"
PORT="5433"
#This is the mininmum id from the network table. Used because low ids have better performance
min_id=57240000233635 


NOW=$(date +%s)

PSQL="psql -U $USERNAME -h $HOST -p $PORT $DATABASE "

echo "Preparing helper views"

$PSQL -c "DROP VIEW routing_norestrictions_view CASCADE"
$PSQL -c "DROP VIEW routing_restrictions_view CASCADE"
$PSQL -c "CREATE OR REPLACE VIEW routing_restrictions_view AS 
 SELECT network.frc AS category, 
	network.id - $min_id::bigint AS id,
	network.gid, 
	network.the_geom, 
	network.x1, 
	network.y1, 
	network.x2, 
	network.y2, 
	(restrictions.target - $min_id::bigint) AS rule, 
	network.f_jnctid_fk AS source, 
	network.t_jnctid_fk AS target, 
	'Infinity'::double precision AS to_cost, 
        CASE network.oneway
            WHEN 'N'::text THEN 'Infinity'::double precision
            WHEN 'TF'::text THEN 'Infinity'::double precision
            ELSE network.minutes
        END AS cost, 
        CASE network.oneway
            WHEN 'N'::text THEN 'Infinity'::double precision
            WHEN 'FT'::text THEN 'Infinity'::double precision
            ELSE network.minutes
        END AS reverse_cost, 
	network.name, 
	network.namelc, 
	network.nametyp, 
	network.rtetyp
   FROM t_network network, ( SELECT b.trpelid[1] AS source, b.trpelid[2] AS target
           FROM ( SELECT array_agg(a.trpelid) AS trpelid
                   FROM t_maneuvers m, ( SELECT pi.id, pi.trpelid
                           FROM t_maneuvers_path_index pi
                          ORDER BY pi.seqnr) a
                  WHERE m.id = a.id AND m.feattyp = 2103 AND m.promantyp = 0
                  GROUP BY a.id) b) restrictions
  WHERE network.id::double precision = restrictions.source;"

$PSQL -c "CREATE OR REPLACE VIEW routing_norestrictions_view AS 
 SELECT network.frc AS category, 
	network.id - $min_id::bigint AS id, 
	network.gid, 
	network.the_geom, 
	network.x1, 
	network.y1, 
	network.x2, 
	network.y2, 
	null::bigint AS rule, 
	network.f_jnctid_fk AS source, 
	network.t_jnctid_fk AS target, 
	'Infinity'::double precision AS to_cost, 
        CASE network.oneway
            WHEN 'N'::text THEN 'Infinity'::double precision
            WHEN 'TF'::text THEN 'Infinity'::double precision
            ELSE network.minutes
        END AS cost, 
        CASE network.oneway
            WHEN 'N'::text THEN 'Infinity'::double precision
            WHEN 'FT'::text THEN 'Infinity'::double precision
            ELSE network.minutes
        END AS reverse_cost, 
	network.name, 
	network.namelc, 
	network.nametyp, 
	network.rtetyp
   FROM t_network network
  WHERE NOT (EXISTS ( SELECT 1
           FROM routing_restrictions_view restr
          WHERE restr.source = network.f_jnctid_fk AND restr.target = network.t_jnctid_fk));"

$PSQL -c "CREATE OR REPLACE VIEW routing_view AS SELECT * FROM routing_norestrictions_view UNION ALL SELECT * FROM routing_restrictions_view;"

$PSQL -c "SELECT * INTO routing_""$NOW"" FROM routing_view;"

$PSQL -c "CREATE INDEX routing_gist_idx_""$NOW"" ON routing_""$NOW"" USING gist (the_geom );"

$PSQL -c "ALTER TABLE routing_""$NOW"" CLUSTER ON routing_gist_idx_""$NOW"";"

$PSQL -c "CREATE INDEX routing_name_idx_""$NOW"" ON routing_""$NOW"" USING btree (name );"

$PSQL -c "CREATE INDEX routing_rule_idx_""$NOW"" ON routing_""$NOW"" USING btree (rule );"

$PSQL -c "CREATE INDEX routing_source_idx_""$NOW"" ON routing_""$NOW"" USING btree (source );"

$PSQL -c "CREATE INDEX routing_target_idx ON routing_""$NOW"" USING btree (target );"

$PSQL -c "CREATE INDEX routing_cost_idx ON routing_""$NOW"" USING btree (cost );"

$PSQL -c "CREATE INDEX routing_reverse_cost_idx ON routing_""$NOW"" USING btree (reverse_cost );"

$PSQL -c "vacuum routing_""$NOW"";"

echo "Replacing old data with new data"

$PSQL -c "CREATE OR REPLACE VIEW routing AS SELECT * FROM routing_"$NOW

echo "Importation successfull."
