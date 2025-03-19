#ifndef _VESCUART_h
#define _VESCUART_h

#include "datatypes.h"
#include "buffer.h"
#include "crc.h"
#include "driver/uart.h"

#define VESC_UART_TAG "VescUart"

#define VESC_UART_NUM UART_NUM_1

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

/** Struct to hold the nunchuck values to send over UART */
typedef struct {
  int valueX;
  int valueY;
  bool upperButton; // valUpperButton
  bool lowerButton; // valLowerButton
} nunchuckPackage;

typedef struct {
  uint8_t major;
  uint8_t minor;
} FWversionPackage;

uint32_t serialAvailable(uart_port_t uart_num);

bool getFWversion(dataPackage *data);
bool getFWversionCAN(dataPackage *data, uint8_t canId);

bool getVescValues(dataPackage *data);
bool getVescValuesCAN(dataPackage *data, uint8_t canId);

void setCurrent(dataPackage *data, float current);
void setCurrentCAN(dataPackage *data, float current, uint8_t canId);

void setBrakeCurrent(dataPackage *data, float brakeCurrent);
void setBrakeCurrentCAN(dataPackage *data, float brakeCurrent, uint8_t canId);

void setRPM(dataPackage *data, float rpm);
void setRPMCAN(dataPackage *data, float rpm, uint8_t canId);

void setDuty(dataPackage *data, float duty);
void setDutyCAN(dataPackage *data, float duty, uint8_t canId);

void sendKeepalive(dataPackage *data);
void sendKeepaliveCAN(dataPackage *data, uint8_t canId);

int32_t packSendPayload(dataPackage *data, uint8_t *payload, int32_t lenPay);
int32_t receiveUartMessage(dataPackage *data, uint8_t *payloadReceived);
bool unpackPayload(uint8_t *message, int32_t lenMes, uint8_t *payload);
bool processReadPacket(dataPackage *data, uint8_t *message);

#endif