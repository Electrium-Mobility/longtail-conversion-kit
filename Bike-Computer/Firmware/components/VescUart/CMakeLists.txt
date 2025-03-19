set(component_srcs "VescUart.c" "crc.c" "buffer.c")

idf_component_register(SRCS "${component_srcs}" PRIV_REQUIRES driver INCLUDE_DIRS ".")