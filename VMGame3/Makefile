OUTPUT_NAME = main
LIBS := -lm -ljvm/java-7-openjdk-amd64/jre/lib/amd64/server/libjvm.so

SRC_DIR := src
OBJ_DIR := obj
OUTPUT_DIR := .

CC := clang
CXX := clang++
ASM := nasm

LD_FLAGS := -O3
C_FLAGS := -O3 -Weverything
CXX_FLAGS := -O3 -Weverything -I/usr/lib/jvm/java-7-openjdk-amd64/include 
ASM_FLAGS := -f elf64

CPP_FILES := $(wildcard $(SRC_DIR)/*.cpp)
CPPOBJ_FILES := $(addprefix $(OBJ_DIR)/,$(notdir $(CPP_FILES:.cpp=.o)))

C_FILES := $(wildcard $(SRC_DIR)/*.c)
COBJ_FILES := $(addprefix $(OBJ_DIR)/,$(notdir $(C_FILES:.c=.o)))

ASM_FILES := $(wildcard $(SRC_DIR)/*.asm)
ASMOBJ_FILES := $(addprefix $(OBJ_DIR)/,$(notdir $(ASM_FILES:.asm=.o)))

#-S -mllvm --x86-asm-syntax=intel
main: $(CPPOBJ_FILES) $(COBJ_FILES) $(ASMOBJ_FILES) 
	$(CXX) $(LD_FLAGS) -o $(OUTPUT_DIR)/$(OUTPUT_NAME) $^ $(LIBS)
	
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) $(C_FLAGS) -c $< -o $@
	
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp
	$(CXX) $(CXX_FLAGS) -c $< -o $@

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.asm
	$(ASM) $(ASM_FLAGS) $< -o $@
	
clean:
	rm -f $(OBJ_DIR)/*
	rm -f $(OUTPUT_DIR)/$(OUTPUT_NAME)
	
