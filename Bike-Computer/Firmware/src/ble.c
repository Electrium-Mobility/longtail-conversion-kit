#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "esp_bt.h"
#include "esp_gap_ble_api.h"
#include "esp_gatts_api.h"
#include "esp_bt_main.h"
#include "esp_gatt_common_api.h"
#include "data.h"
#include "ble.h"

uint16_t service_handle;
esp_gatt_if_t gatts_if;
uint16_t conn_id;
bool is_connected = false;
char* bitmap = "";
uint8_t service_uuid[16] = {SERVICE_UUID};
uint8_t eta_uuid[16] = {ETA_UUID};
uint8_t direction_uuid[16] = {DIRECTION_UUID};

Data eta, direction;
Data etaRelative, etaAbsolute, etaDistance, direction, distanceToNextDirection;

Characteristic characteristics[NUM_CHARACTERISTICS];

// Advertising parameters
esp_ble_adv_params_t advParams = {
    .adv_int_min = 0x20,      // 20ms
    .adv_int_max = 0x40,      // 40ms
    .adv_type = ADV_TYPE_IND, // Peripheral requests connection to central
    .own_addr_type = BLE_ADDR_TYPE_PUBLIC,
    .channel_map = ADV_CHNL_ALL,
    .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY, // Scan, connection requests from any device
};

esp_gatt_srvc_id_t serviceID = {
    .id = {
        .uuid = {
            .len = ESP_UUID_LEN_128,
            .uuid = {
                .uuid128 = {SERVICE_UUID},
            },
        },
        .inst_id = 0,
    },
    .is_primary = true,
};

void initializeBLECharacteristics()
{
  characteristics[0].uuid.len = ESP_UUID_LEN_128;
  memcpy(characteristics[0].uuid.uuid.uuid128, eta_uuid, 16);
  characteristics[0].data = &eta;

  characteristics[1].uuid.len = ESP_UUID_LEN_128;
  memcpy(characteristics[1].uuid.uuid.uuid128, direction_uuid, 16);
  characteristics[1].data = &direction;
}

Characteristic *findCharacteristicByUUID(uint8_t *uuid)
{
  for (int i = 0; i < NUM_CHARACTERISTICS; ++i)
  {
    if (memcmp(characteristics[i].uuid.uuid.uuid128, uuid, 16) == 0)
    {
      return &characteristics[i];
    }
  }
  return NULL;
}

Characteristic *findCharacteristicByHandle(uint16_t handle)
{
  for (int i = 0; i < NUM_CHARACTERISTICS; ++i)
  {
    if (memcmp(&characteristics[i].handle, &handle, 2) == 0)
    {
      return &characteristics[i];
    }
  }
  return NULL;
}

void gapEventHandler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param)
{
  if (event == ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT)
  {
    ESP_LOGI(BLE_TAG, "ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT");
    esp_ble_gap_start_advertising(&advParams);
  }
}

void gattsEventHandler(esp_gatts_cb_event_t event, esp_gatt_if_t gattc_if, esp_ble_gatts_cb_param_t *param)
{
  switch (event)
  {
  // Register GATT interface
  case ESP_GATTS_REG_EVT:
    ESP_LOGI(BLE_TAG, "ESP_GATTS_REG_EVT");
    if (param->reg.status == ESP_GATT_OK)
    {
      gatts_if = gattc_if;

      // Create the service
      esp_ble_gatts_create_service(gatts_if, &serviceID, 5);
    }
    else
    {
      ESP_LOGI(BLE_TAG, "Reg app failed, app_id %04x, status %d",
               param->reg.app_id,
               param->reg.status);
      return;
    }
    break;

  // Create and start BLE service
  case ESP_GATTS_CREATE_EVT:
    ESP_LOGI(BLE_TAG, "Creating BLE service");
    if (param->create.status == ESP_GATT_OK)
    {
      service_handle = param->create.service_handle;
      // Start the service
      esp_ble_gatts_start_service(service_handle);

      // Add characteristics
      for (int i = 0; i < NUM_CHARACTERISTICS; i++)
      {
        esp_err_t err = esp_ble_gatts_add_char(service_handle,
                                               &(characteristics[i].uuid),
                                               ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE,
                                               ESP_GATT_CHAR_PROP_BIT_READ | ESP_GATT_CHAR_PROP_BIT_WRITE,
                                               NULL, NULL);
        if (err != ESP_OK)
        {
          ESP_LOGI(BLE_TAG, "Could not add characteristic 0x%02X, error code: %x", *characteristics[i].uuid.uuid.uuid128, err);
        }
      }
    }
    break;

  // Add characteristics to service
  case ESP_GATTS_ADD_CHAR_EVT:
    ESP_LOGI(BLE_TAG, "Adding characteristic");
    if (param->add_char.status == ESP_GATT_OK)
    {
      ESP_LOGI(BLE_TAG, "About to create characteristic with UUID 0x%02X, Handle %d", *param->add_char.char_uuid.uuid.uuid128, param->add_char.attr_handle);
      Characteristic *characteristic = findCharacteristicByUUID(param->add_char.char_uuid.uuid.uuid128);
      if (characteristic)
      {
        characteristic->handle = param->add_char.attr_handle;
        characteristic->uuid.len = ESP_UUID_LEN_128;
        memcpy(characteristic->uuid.uuid.uuid128, param->add_char.char_uuid.uuid.uuid128, 16);
        esp_err_t add_descr_ret = esp_ble_gatts_add_char_descr(
            service_handle,
            &characteristic->uuid,
            ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE,
            NULL,
            NULL);
        if (add_descr_ret != ESP_OK)
        {
          ESP_LOGE(BLE_TAG, "add char descr failed, error code = %x", add_descr_ret);
        }
        else
        {
          ESP_LOGI(BLE_TAG, "Characteristic added with handle %d", characteristic->handle);
        }
      }
    }
    else
    {
      ESP_LOGI(BLE_TAG, "Could not add characteristic: %s", esp_err_to_name(param->add_char.status));
    }
    break;

  // Write to a characteristic
  case ESP_GATTS_WRITE_EVT:
    ESP_LOGI(BLE_TAG, "Writing to characteristic");
    gatts_if = gattc_if;

    if (param->write.handle)
    {
      Characteristic *characteristic = findCharacteristicByHandle(param->write.handle);
      if (characteristic)
      {
        if (!param) {
          ESP_LOGE(BLE_TAG, "param is NULL!");
          return;
      }
        // Copy the data and ensure null termination
        char value[256] = {0};
        memcpy(value, param->write.value, param->write.len < 255 ? param->write.len : 255);

        // Set payload
        memcpy(characteristic->data->payload, value, sizeof(value));
        characteristic->data->updated = true;

        ESP_LOGI(BLE_TAG, "Write to characteristic 0x%02X received: %s", *characteristic->uuid.uuid.uuid128, value);
        gatts_if = gattc_if;

        esp_err_t ret = esp_ble_gatts_send_response(gattc_if, param->write.conn_id, param->write.trans_id, ESP_GATT_OK, NULL);
        ESP_LOGI(BLE_TAG, "%s", esp_err_to_name(ret));
        ESP_LOGI(BLE_TAG, "Response sent");
      }
      else {
        ESP_LOGI(BLE_TAG, "No characteristic, handle is %d", param->write.handle);
      }
    }
    else
    {
      ESP_LOGI(BLE_TAG, "Write handle is null");
    }
    break;

  case ESP_GATTS_MTU_EVT:
    ESP_LOGI(BLE_TAG, "MTU changed to %d", param->mtu.mtu);
    esp_err_t ret = esp_ble_gatt_set_local_mtu(param->mtu.mtu);
    if (ret != ESP_OK)
    {
      ESP_LOGI(BLE_TAG, "MTU could not be set: %s", esp_err_to_name(ret));
    }
    break;

  // Handle connection/disconnection
  case ESP_GATTS_CONNECT_EVT:
  
    conn_id = param->connect.conn_id;
    gatts_if = gattc_if;
    is_connected = true;

    ESP_LOGI(BLE_TAG, "Connection Details:");
    ESP_LOGI(BLE_TAG, "Connection ID: %d", conn_id);
    ESP_LOGI(BLE_TAG, "Interface: %d", gatts_if);
    ESP_LOGI(BLE_TAG, "Remote Address Type: %d", param->connect.ble_addr_type);
    ESP_LOGI(BLE_TAG, "Device connected");
    break;

  case ESP_GATTS_DISCONNECT_EVT:
    ESP_LOGI(BLE_TAG, "Disconnection Reason: 0x%02X", param->disconnect.reason);
    is_connected = false;
    ESP_LOGI(BLE_TAG, "Device disconnected");

    // Restart advertising when disconnected
    esp_ble_gap_start_advertising(&advParams);
    break;

  default:
    break;
  }
}

void initBLE()
{
  ESP_LOGI(BLE_TAG, "Initializing BLE");

  // Initialize NVS
  esp_err_t ret = nvs_flash_init();
  if (ret != ESP_OK)
  {
    ESP_ERROR_CHECK(nvs_flash_erase());
    ret = nvs_flash_init();
  }
  ESP_ERROR_CHECK(ret);

  ESP_LOGI(BLE_TAG, "NVS flash initialized");

  // Initialize Bluetooth controller
  esp_bt_controller_config_t btConfig = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
  ret = esp_bt_controller_init(&btConfig);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "Bluetooth controller initialization failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Bluetooth controller initialized");

  ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "Bluetooth controller enable failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Bluetooth controller enabled");

  // Initialize Bluetooth host
  ret = esp_bluedroid_init();
  ESP_LOGI(BLE_TAG, "%s", esp_err_to_name(ret));
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "Bluedroid init failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Bluetooth host initialized");

  ret = esp_bluedroid_enable();
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "Bluedroid enable failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Bluedroid enabled");

  // Register callbacks
  ret = esp_ble_gap_register_callback(gapEventHandler);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "GAP register callback failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "GAP callback registered");

  ret = esp_ble_gatts_register_callback(gattsEventHandler);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "GATTS register callback failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "GATTS callback registered");

  // Register application
  ret = esp_ble_gatts_app_register(0);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "GATTS app register failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Application registered");

  // Set advertising data
  esp_ble_adv_data_t advData = {
      .set_scan_rsp = false,
      .include_name = true,
      .include_txpower = false,
      .min_interval = 0x0006, // 7.5ms
      .max_interval = 0x0010, // 20ms
      .appearance = 0x00,     // No icon
      .manufacturer_len = 0,
      .p_manufacturer_data = NULL,
      .service_data_len = 0,
      .p_service_data = NULL,
      .service_uuid_len = ESP_UUID_LEN_128,
      .p_service_uuid = service_uuid,
      .flag = (ESP_BLE_ADV_FLAG_GEN_DISC | ESP_BLE_ADV_FLAG_BREDR_NOT_SPT), // Discoverable, BLE only
  };

  ESP_LOGI(BLE_TAG, "Advertising data length: %d", advData.service_uuid_len);

  ret = esp_ble_gap_config_adv_data(&advData);
  if (ret != ESP_OK)
  {
    ESP_LOGE(BLE_TAG, "Config adv data failed: %s", esp_err_to_name(ret));
    return;
  }

  ESP_LOGI(BLE_TAG, "Config adv data done");

  initializeBLECharacteristics();
  ESP_LOGI(BLE_TAG, "BLE initialization complete");
}