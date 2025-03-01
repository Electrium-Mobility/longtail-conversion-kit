#ifndef DATA_H
#define DATA_H

typedef struct {
    char* payload;
    bool updated;
} Data;

typedef struct {
    uint16_t handle;
    Data* data;
    esp_bt_uuid_t uuid;
} Characteristic;

#endif