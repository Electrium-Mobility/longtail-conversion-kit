set(component_srcs "VescCAN.c" "crc.c" "buffer.c")

idf_component_register(SRCS "${component_srcs}" PRIV_REQUIRES driver INCLUDE_DIRS ".")