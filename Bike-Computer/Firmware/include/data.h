#ifndef DATA_H
#define DATA_H

#include "esp_bt_defs.h"

typedef struct {
    char payload[256];
    bool updated;
} Data;

typedef struct {
    uint16_t handle;
    Data* data;
    esp_bt_uuid_t uuid;
} Characteristic;

#endif