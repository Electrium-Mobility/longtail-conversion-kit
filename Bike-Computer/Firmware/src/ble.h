#ifndef BLE_H
#define BLE_H

//All UUIDs are little endian so SERVICE UUID is "c8a19548-8efa-4143-87eb-5e85ecefc852"
#define SERVICE_UUID 0x52, 0xc8, 0xef, 0xec, 0x85, 0x5e, 0xeb, 0x87, 0x43, 0x41, 0xfa, 0x8e, 0x48, 0x95, 0xa1, 0xc8

#define ETA_UUID 0x6c, 0xc8, 0x7f, 0xfa, 0x65, 0x9d, 0x43, 0xba, 0x61, 0x4c, 0x02, 0xbc, 0x89, 0x3d, 0xf7, 0x9a
#define DIRECTION_UUID 0x51, 0xb8, 0x44, 0xf5, 0x72, 0xea, 0x29, 0xbb, 0x12, 0x48, 0xcf, 0x98, 0xbe, 0x98, 0xee, 0xb2

#define NUM_CHARACTERISTICS 2

#define BLE_TAG "BLE"

#include "esp_bt.h"
#include "esp_gap_ble_api.h"
#include "esp_gatts_api.h"
#include "esp_gatt_common_api.h"
#include "data.h"

//Initialize all characteristic structs with respective UUIDs and data pointers
void initializeBLECharacteristics();
void initBLE();
void cleanup_for_ble();

//Search characteristic array
Characteristic* findCharacteristicByUUID(uint8_t* uuid);
Characteristic* findCharacteristicByHandle(uint16_t handle);

//Handles device discovery, connection
void gapEventHandler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param);

//Handles data transfer
void gattsEventHandler(esp_gatts_cb_event_t event, esp_gatt_if_t gattc_if, esp_ble_gatts_cb_param_t *param);

#endif