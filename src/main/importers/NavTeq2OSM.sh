#!/bin/sh

if [ -e "$1" ]; then
  echo "NavTeq2OSM $1 -> $2"
else
  echo "Usage: NavTeq2OSM path_to_shapes output_file"
  exit 1
fi

main()
{
  SHP_PATH=$1
  OUT_FILE=$2

  echo "Collecting data into memory"

  #dbfdump -m $SHP_PATH/Streets.shp

  echo "Writing file $OUT_FILE"

  rm -f $OUT_FILE

  writeHeader $OUT_FILE
  writeNodes $OUT_FILE $nodes
  writeRelations $OUT_FILE $relations
  writeWays $OUT_FILE $ways
  echo "</osm>" >> $OUT_FILE

  echo "Done"
}

writeHeader(){
  echo "<?xml version='1.0' encoding='UTF-8'?>" >> $1
  echo "<osm version='0.6' generator='navteq2osm'>" >> $1
}

writeNodes(){
  if [ -e "$2" ]; then
    echo "Writing nodes"
    for i in ${2[@]}
    do
      echo "   <node $i/>" >> $1
    done
  fi
}

writeRelations(){
  if [ -e "$2" ]; then
    echo "Writing relations"
    for i in ${2[@]}
    do
      echo "   <relation/>" >> $1
    done
  fi
}

writeWays(){
  if [ -e "$2" ]; then
    echo "Writing ways"
    for i in ${2[@]}
    do
      echo "   <way/>" >> $1
    done
  fi
}

main $1 $2