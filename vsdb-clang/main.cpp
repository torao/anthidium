#include <iostream>
#include "src/core.h"
#include "src/vsdb.h"

int main() {
    try{
        vsdb::Database db("sample.vsdb");
        db.open();
    } catch(std::runtime_error& ex){
        std::cout << "ERROR: " << ex.what() << std::endl;
    }
    return 0;
}