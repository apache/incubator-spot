 #!/bin/bash

source /etc/spot.conf

#  copy solution files to all nodes
for d in "${NODES[@]}" 
do
    rsync -v -a --include='target' --include='target/scala-2.10' --include='target/scala-2.10/spot-ml-assembly-1.1.jar' \
      --include 'top-1m.csv' --include='*.sh' \
      --exclude='*' .  $d:${LUSER}/ml
done

