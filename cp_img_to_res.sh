#!/bin/bash

DROPBOX_PATH=/Users/kingori/Dropbox/@_JJD/Hackfair/Android/02_png
TEMP_PATH=~/temp_ihatecolor

function mv_to_dir() {
    mkdir $2
    find . -type f -name "*_$1.png" |while read -r file;
    do
        nfile=`echo $file | sed "s/_$1\.png/\.png/g"`;
        mv "$file" "$2/$nfile" 2>/dev/null;
    done
    find . -type f -name "*_$1.9.png" |while read -r file;
        do
            nfile=`echo $file | sed "s/_$1\.9\.png/\.9\.png/g"`;
            mv "$file" "$2/$nfile" 2>/dev/null;
        done
}

rm -rf $TEMP_PATH
mkdir $TEMP_PATH
mkdir $TEMP_PATH/original

#copy and flatten directory
cp -r $DROPBOX_PATH $TEMP_PATH/original
for i in `find $TEMP_PATH/original -type d -mindepth 1 |sort -r`; do mv $i/* $TEMP_PATH/;rm -r $i; done
mv $TEMP_PATH/original/* $TEMP_PATH
rm -rf $TEMP_PATH/original

pushd $TEMP_PATH

#mv based on density
mv_to_dir kor_xxh drawable-ko-xxhdpi
mv_to_dir kor_xh drawable-ko-xhdpi
mv_to_dir kor_h drawable-ko-hdpi
mv_to_dir kor_m drawable-ko-mdpi
mv_to_dir xxh drawable-xxhdpi
mv_to_dir xh drawable-xhdpi
mv_to_dir h drawable-hdpi
mv_to_dir m drawable-mdpi


#del zip file
for i in `find . -maxdepth 1 -type f -name "*.zip"`; do rm $i; done

#mv rest to hdpi
for i in `find . -maxdepth 1 -type f`; do mv $i drawable-hdpi; done


popd


#copy to res
cp -r $TEMP_PATH/ ./res
#remove temp
rm -rf $TEMP_PATH

#handle locale files
#mv res/drawable-hdpi/img_kakaogroup_kr.png res/drawable-ko-hdpi/img_kakaogroup.png
#mv res/drawable-mdpi/img_kakaogroup_kr.png res/drawable-ko-mdpi/img_kakaogroup.png
#mv res/drawable-xhdpi/img_kakaogroup_kr.png res/drawable-ko-xhdpi/img_kakaogroup.png
#mv res/drawable-xxhdpi/img_kakaogroup_kr.png res/drawable-ko-xxhdpi/img_kakaogroup.png

exit
