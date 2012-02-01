#!/bin/bash

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


  #nodes and ways
  declare -a ways
  for line in $(dbfdump -m $SHP_PATH/Streets.shp)
  do

    case $next in
      id)
	id=$line
	ways[$id]=""$line
	;;
      tag_name)
	ways[$id]=${ways[$id]}";name="$line
	;;
    esac

    #echo $line
    case $line in
      LINK_ID*)
	next="id"
	;;
      ST_NAME*)
	next="tag_name"
      ;;
      *)
	next="none"
      ;;
    esac
  done

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
  ways=$2
  echo "writeWays"
  for w in ${ways[@]}
  do
    IFS=";"
    for word in $w; do
      case $word in
	*=*)
	  index=$(expr index "$word" "=")
	  echo "      <tag k='${word:0:$index-1}' v='${word:$index}'/>" >> $1
	  ;;
	*)
	  echo "   <way id='$word'>" >> $1
	  ;;
      esac
    done
    echo "   </way>" >> $1
  done
}

main $1 $2