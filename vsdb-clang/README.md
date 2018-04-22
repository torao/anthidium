
https://askubuntu.com/questions/355565/how-do-i-install-the-latest-version-of-cmake-from-the-command-line

```
$ version=3.11
$ build=0
$ mkdir ~/temp
$ cd ~/temp
$ wget https://cmake.org/files/v$version/cmake-$version.$build.tar.gz
$ tar -xzvf cmake-$version.$build.tar.gz
$ cd cmake-$version.$build/
./bootstrap
make -j4
sudo make install

$ cmake --version
```