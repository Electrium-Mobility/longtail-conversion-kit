#ifndef _VESCCAN_h
#define _VESCCAN_h

#include "datatypes.h"
#include "buffer.h"
#include "crc.h"
#include "driver/uart.h"

#define VESC_CAN_TAG "VescCAN"

#define ESC_ID 116

/** Struct to store the telemetry data returned by the VESC */
typedef struct {
  float avgMotorCurrent;
  float avgInputCurrent;
  float dutyCycleNow;
  float rpm;
  float inpVoltage;
  float ampHours;
  float ampHoursCharged;
  float wattHours;
  float wattHoursCharged;
  uint32_t tachometer;
  uint32_t tachometerAbs;
  float tempMosfet;
  float tempMotor;
  float pidPos;
  uint8_t id;
  mc_fault_code error;
} dataPackage;

bool sendMessage(uint8_t controllerId, CAN_PACKET_ID cmd, uint8_t *data, uint8_t len);
bool receiveMessage(uint8_t *controllerId, CAN_PACKET_ID *cmd, uint8_t *data, uint8_t *len);

bool getVescValues(dataPackage* data);
bool getVescValuesCAN(dataPackage *data, uint8_t canId);

void setCurrent(dataPackage *data, float current);
void setCurrentCAN(dataPackage *data, float current, uint8_t canId);

void setRPM(dataPackage *data, float rpm);
void setRPMCAN(dataPackage *data, float rpm, uint8_t canId);

void setDuty(dataPackage *data, float duty);
void setDutyCAN(dataPackage *data, float duty, uint8_t canId);

void setBrakeCurrent(dataPackage *data, float brakeCurrent);
void setBrakeCurrentCAN(dataPackage *data, float brakeCurrent, uint8_t canId);

#endif