if [ $# -ne 2 ]
then
	echo "Usage : $0 repository_dir section_size"
	exit 1
fi
cd $1
git checkout -- ./
git clean -f -d
git checkout master
lines=`git log --oneline | wc -l`
size=$2
i=0
bound=$[$lines - $size]
while [ $i -le $bound ]
do
	start=$[$i + 1]
	i=$[$i + $size]
	end=$i
	startID=`git log --pretty=format:%h --date-order | sed -n ${start}p`
	endID=`git log --pretty=format:%h --date-order | sed -n ${end}p`
	echo -s $startID -e $endID
done
start=$[$i+1]
end=$lines
startID=`git log --pretty=format:%h --date-order | sed -n ${start}p`
endID=`git log --pretty=format:%h --date-order | sed -n ${end}p`
echo -s $startID -e $endID
