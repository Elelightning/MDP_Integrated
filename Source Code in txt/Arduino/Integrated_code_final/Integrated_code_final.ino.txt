#include <DualVNH5019MotorShield.h>
#include <PinChangeInterrupt.h>
#include <PID_v1.h>
#include <SharpIR.h>
#include <math.h>

#define LEFT_ENCODER 11 //left motor encoder A to pin 11
#define RIGHT_ENCODER 3 //right motor encoder A to pin 3
#define inputB 10 //right motor speed input
#define inputA 9; //left motor speed input

//creating sensor objects
SharpIR sharp1(A0, 25, 93, 0, 1080);
SharpIR sharp2(A1, 25, 93, 0, 1080);
SharpIR sharp3(A2, 25, 93, 0, 1080);
SharpIR sharp4(A3, 25, 93, 0, 1080);
SharpIR sharp5(A4, 25, 93, 0, 1080);
SharpIR sharp6(A5, 25, 93, 0, 20150);

DualVNH5019MotorShield md;

//default for exploration
int SPEED_L = 300;
int SPEED_R = 325;
double TURN_L = 8.57; //new bat pos 6.2V Batt A Albert PB // Bat B 8.58 // Bat A 8.6
double TURN_R = 8.54; //new bat pos 6.2V Batt A Albert PB // Bat B 8.56 // Bat A 8.58

double leftEncoderValue = 0;
double rightEncoderValue = 0;
double difference;
double Setpoint, Input, Output;
double turnLeftEncoderValue, turnRightEncoderValue, startLeftEncoderValue, startRightEncoderValue;
bool fastestPath = false;
bool forward_error = false;

PID myPID(&leftEncoderValue, &Output, &rightEncoderValue, 0.5, 0, 0, DIRECT);

char piCommand_buffer[512], readChar, instruction, flushChar;
int i, arg;

void setup() {
  Serial.begin(57600);
  Serial.setTimeout(0);

  // set up motor & PID
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 11 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 3 as input
  attachPCINT(digitalPinToPCINT(LEFT_ENCODER), leftEncoderInc, HIGH);
  attachPCINT(digitalPinToPCINT(RIGHT_ENCODER), rightEncoderInc, HIGH);
  myPID.SetOutputLimits(-50, 50);
  myPID.SetMode(AUTOMATIC);


  //set sensor pins
  pinMode (A0, INPUT);
  pinMode (A1, INPUT);
  pinMode (A2, INPUT);
  pinMode (A3, INPUT);
  pinMode (A4, INPUT);
  pinMode (A5, INPUT);
}

void loop() {
  int buffer_size = sizeof(piCommand_buffer) / sizeof(*piCommand_buffer);

  i = 0, arg = 0;

  // type character for command
  // for forward & backward, type F or B with distance / 10
  while (1) {
    if (Serial.available()) {
      readChar = Serial.read();
      if (isAlphaNumeric(readChar) or readChar == '|') {
        piCommand_buffer[i] = readChar;
        i++;

        if (readChar == '#') {
          return;
        }

        if (readChar == '|' || i >= buffer_size) {
          i = 1;
          break;
        }
      }
    }
  }

  instruction = piCommand_buffer[0];

  // multiply distance argument by 10
  while (piCommand_buffer[i] != '|' && i < buffer_size) {
    // subtract from ASCII value to get equivalent integer value
    arg = arg + (piCommand_buffer[i] - 48);
    arg *= 10;
    i++;
  }

  switch (instruction) {
    // fastest path mode
    case 'F':
      fastestPath = true;
      SPEED_L = 340;
      SPEED_R = 350;
      TURN_L = 8.50;
      TURN_R = 8.53;
      //f_cal = 3.00; // 6.2V Batt A
      break;

    // exploration mode
    case 'E':
      fastestPath = false;
      break;

    case '#':
      Serial.print("end");
      shutdown();
      break;

    // rotate left 90
    case 'A':
      //Serial.print("left");
      rotateLeft(90);
      //Serial.print("A D|");
      break;

    // move forward
    case 'W':
      //Serial.print("straight");
      forward(arg);
      /*if (fastestPath == false)
      {
        checkDistance();
      }*/

      //Serial.print("W D|");
      break;

    // move backwardd
    case 'S':
      //Serial.print("back");
      backward(arg);
      //Serial.print("S D|");
      break;

    // rotate right 90
    case 'D':
      //Serial.print("right");
      rotateRight(90);
      //Serial.print("D D|");
      break;

    // call sensor
    case 'Z':
      //Serial.print("sense");
      //sense();
      // Serial.print("Z D|");
      break;

    // adjust distance
    case 'X':
      //Serial.print("adjust distance");
      adjustDistance();
      //Serial.print("X D|");
      break;

    // align angle
    case 'C':
      //Serial.print("align angle");
      alignAngle();
      //Serial.print("C D|");
      break;

    // align robot with front wall
    case 'V':
      //   .print("align front");
      alignFront();
      //Serial.print("V D|");
      break;

    // align robot with right wall
    case 'B':
      //Serial.print("align right");
      alignRight();
      //Serial.print("B D|");
      break;

    case 'L':
      //corner combo
      alignAngle();
      rotateRight(90);
      alignFront();
      adjustDistance();
      rotateLeft(90);
      alignRight();
      alignRight();
      alignRight();
      //Serial.print("L D|")
      break;
  }

  if (fastestPath == false)
  {
    // calibration for setup in exploration mode
    if (instruction != 'E') {
      delay(100);
      alignRight();
      delay(20);
      tooCloseToWall();
      delay(20);
      sense();
    }
  }
  else
  {
    //delay(100);
  }
}

void rotateLeft(double angle) {
  startLeftEncoderValue = leftEncoderValue;
  startRightEncoderValue = rightEncoderValue;
  double target_Tick = 0;

  if (angle <= 90) target_Tick = angle * TURN_L; //9.1
  else if (angle <= 180 ) target_Tick = angle * 8.80;   //tune 180
  else if (angle <= 360 ) target_Tick = angle * 8.65;
  else target_Tick = angle * 8.9;

  while (leftEncoderValue < startLeftEncoderValue + target_Tick ) {
    md.setSpeeds(-(SPEED_L + Output), (SPEED_R - Output));
    myPID.Compute();
  }
  md.setBrakes(400, 400);
  delay(5);
}

void rotateRight(double angle) {
  startLeftEncoderValue = leftEncoderValue;
  startRightEncoderValue = rightEncoderValue;

  double target_Tick = 0;

  if (angle <= 90) target_Tick = angle * TURN_R; //8.96
  else if (angle <= 180 ) target_Tick = angle * 8.80;   //tune 180
  else if (angle <= 360 ) target_Tick = angle * 8.65;
  else target_Tick = angle * 8.9;

  while (rightEncoderValue < startRightEncoderValue + target_Tick ) {
    md.setSpeeds((SPEED_L + Output), -(SPEED_R - Output));
    myPID.Compute();
  }
  md.setBrakes(400, 400);
  delay(5);
}

void forward(double dist)
{
  double fwd_dist;
  double offset = 0;
  double last_tick_R = rightEncoderValue;
  int duration1, duration2;
  int crash_threshold;

  if (fastestPath == true)
  {
    // increase the speed when moving forward
    SPEED_L = 400;
    SPEED_R = 400;
    int newarg = dist / 10;
    if (newarg >= 0 && newarg <= 3)
    {
      //f_cal = 3.15; // 5.8-6.1V Batt Be
      fwd_dist = 58 * dist;
    }
    else if (newarg > 3 && newarg <= 6)
    {
      //f_cal = 3.03; // 5.8-6.1V Batt Be
      fwd_dist = 57.066 * dist;
    }
    else if (newarg > 6 && newarg <= 10)
    {
      //f_cal = 2.95; // 5.8-6.1V Batt Be
      fwd_dist = 58.668 * dist;
    }
    else if (newarg > 10 && newarg <= 18)
    {
      //f_cal = 2.95; // 5.8-6.1V Batt Be
      fwd_dist = 62.668 * dist;
    }
    else
    {
      //f_cal = 2.9;
      fwd_dist = 71.714 * dist;
    }
  } else {
    // (562.25 * 10) / (4 * 3.1416);
    // only move 10 cm for exploration mode
    fwd_dist = 541.786;
  }

  //fwd_dist = (562.25 * dist) / (f_cal * 3.1416);
  // for emergency brake
  if (fastestPath == false) {
    crash_threshold = 0.8 * fwd_dist;
  } else {
    crash_threshold = 0.4 * fwd_dist;
  }

  double fwd_L_encoder = leftEncoderValue;
  double fwd_R_encoder = rightEncoderValue;
  
  while ((leftEncoderValue < fwd_L_encoder + fwd_dist) || (rightEncoderValue < fwd_R_encoder + fwd_dist)) {
    if ((rightEncoderValue - last_tick_R) >= 10 || rightEncoderValue == 0 || rightEncoderValue == last_tick_R) {
      last_tick_R = rightEncoderValue;
      offset += 0.1;
    }
    if (myPID.Compute() || rightEncoderValue == last_tick_R) {
      if (offset >= 1) {
        md.setSpeeds((SPEED_L + Output), (SPEED_R - Output));
      }
      else {
        // acceleration
        md.setSpeeds(offset * (SPEED_L + Output), offset * (SPEED_R - Output));
      }

    }

    // check when to apply emergency brakes
    if ((fwd_L_encoder + fwd_dist - leftEncoderValue < crash_threshold) && (int(fwd_L_encoder + fwd_dist - leftEncoderValue) % 10 == 0)) {
      double sense1 = ir_sense1();
      double sense2 = ir_sense2();
      double sense3 = ir_sense3();
      
      if (fastestPath == true) {
        if (sense1 < 15 || sense3 < 15  || sense2 < 11.8) {
          break;
        }
      } else {
        if (sense1 < 12.5 || sense3 < 12.5  || sense2 < 9.3) {
          /*if(fwd_L_encoder + fwd_dist - leftEncoderValue > 0.3 * fwd_dist)
          {
            forward_error = true;
          }*/
          break;
        }
      }
    }
  }

  md.setBrakes(400, 400);

  if (fastestPath == true) {
    // reset speed for slower turning
    SPEED_L = 340;
    SPEED_R = 350;
  }
  delay(5);
}

void backward(double dist)
{
  double bwd_dist;
  bwd_dist = (562.25 * dist) / (3.1 * 3.1416);

  double bwd_L_encoder = leftEncoderValue;
  double bwd_R_encoder = rightEncoderValue;

  while (1) {
    md.setSpeeds(-(SPEED_L + Output), -(SPEED_R - Output));
    myPID.Compute();

    if ((leftEncoderValue > bwd_L_encoder + bwd_dist) || (rightEncoderValue > bwd_R_encoder + bwd_dist)) {
      md.setBrakes(400, 400);
      delay(5);
      break;
    }
  }

}

// check if the distance in front of the robot is acceptable
// move forward/ backward as necessary
void checkDistance()
{
  double dis1 = ir_sense1();
  double dis2 = ir_sense2();
  double dis3 = ir_sense3() - 0.5;
  if ((dis1 < 10.5) && (dis2 < 7.3) && (dis3 < 10.5)
     )
  {
    adjustDistance();
    alignFront();
  }
}

// check if the robot is too close to the right wall
void tooCloseToWall()
{
  double dis4 = ir_sense4();
  double dis5 = ir_sense5();
  if ((dis4 < 7.4) && (dis5 < 7.4))
  {
    rotateRight(90);
    adjustDistance();
    rotateLeft(90);
  }
  else if ((dis4 > 8.5 && dis4 < 13) && (dis5 > 8.5 && dis5 < 13))
  {
    rotateRight(90);
    adjustDistance();
    rotateLeft(90);
  }
}

void sense() {
  double dis1 = ir_sense1();
  double dis2 = ir_sense2();
  double dis3 = ir_sense3(); //maybe remove for the sending to algo
  double dis4 = ir_sense4();
  double dis5 = ir_sense5();
  double dis6 = ir_sense6();

  if (isinf(dis1))
  {
    dis1 = 800;
  }
  if (isinf(dis2))
  {
    dis2 = 800;
  }
  if (isinf(dis3))
  {
    dis3 = 800;
  }
  if (isinf(dis4))
  {
    dis4 = 800;
  }
  if (isinf(dis5))
  {
    dis5 = 800;
  }
  if (isinf(dis6))
  {
    dis6 = 800;
  }

  Serial.print(dis1);
  Serial.print("|");
  Serial.print(dis2);
  Serial.print("|");
  Serial.print(dis3);
  Serial.print("|");
  Serial.print(dis4);
  Serial.print("|");
  Serial.print(dis5);
  Serial.print("|");
  Serial.print(dis6);
  if(forward_error ==true)
  {
    Serial.print("|");
    Serial.println("0");
  } else {
    Serial.print("|");
    Serial.println("1");
  }
  forward_error = false;
}

double ir_sense1() {
  double dis1 = sharp1.distance(); // this returns the distance to the object you're measuring
  // working range 10 - 30cm
  return dis1;
}

double ir_sense2() {
  double dis2 = sharp2.distance();
  // working range 10 - 50 cm
  return dis2;
}

double ir_sense3() {
  double dis3 = sharp3.distance();
  return dis3;
}

double ir_sense4() {
  double dis4 = sharp4.distance();
  // working range 10 - 50 cm
  return dis4;
}

double ir_sense5() {
  double dis5 = sharp5.distance();
  // working range 10 - 40 cm
  return dis5;
}

double ir_sense6() {
  double dis6 = sharp6.distance();
  // working range 20 - 70 cm
  if (29 <= dis6 && dis6 <= 42) {
    dis6 += 1;
  } else if (43 <= dis6 && dis6 <= 52) {
    dis6 += 1;
  } else if (53 <= dis6 && dis6 <= 65) {
    dis6 += 1;
  } else if (dis6 >= 65) {
    dis6 += 1;
  }
  return dis6;
}

void leftEncoderInc(void) {
  leftEncoderValue++;
}

void rightEncoderInc(void) {
  rightEncoderValue++;
}

// robot moves backwards if the front is too close to the wall
void adjustDistance() {

  double ad_L_encoder = leftEncoderValue;
  double ad_R_encoder = rightEncoderValue;
  double sensor_R_dis = ir_sense1();
  double sensor_L_dis = ir_sense3() - 0.5;

  moveCloserToWall(sensor_R_dis, sensor_L_dis);
  while ((sensor_R_dis < 8.9) || (sensor_L_dis < 8.9)) {
    md.setSpeeds(-75, -75);
    sensor_R_dis = ir_sense1();
    sensor_L_dis = ir_sense3() - 0.5;
  }
  md.setBrakes(400, 400);
  leftEncoderValue = ad_L_encoder;
  rightEncoderValue = ad_R_encoder;

}

// robot moves forward if the robot is too far away from the wall
void moveCloserToWall(double sensor_R_dis, double sensor_L_dis) {
  double mw_L_encoder = leftEncoderValue;
  double mw_R_encoder = rightEncoderValue;

  while ((sensor_R_dis > 9.5) || (sensor_L_dis > 9.5)) {
    //forward(0.2);
    md.setSpeeds(100, 100);
    //delay(10);
    sensor_R_dis = ir_sense1();
    sensor_L_dis = ir_sense3() - 0.5;
  }
  leftEncoderValue = mw_L_encoder;
  rightEncoderValue = mw_R_encoder;
}

void alignAngle() {

  double cal_L_encoder = leftEncoderValue;
  double cal_R_encoder = rightEncoderValue;

  alignFront();

  adjustDistance();

  alignRight();

  leftEncoderValue = cal_L_encoder;
  rightEncoderValue = cal_R_encoder;

}

// ensure that the front of the robot is straight
void alignFront() {
  double cal_L_encoder = leftEncoderValue;
  double cal_R_encoder = rightEncoderValue;

  double rad2deg = 180 / 3.14159;

  double sensor_R_dis;
  double sensor_L_dis;

  int count = 0;

  double sensorDiff;

  sensor_R_dis = ir_sense1();
  sensor_L_dis = ir_sense3();

  sensorDiff = abs(sensor_R_dis - sensor_L_dis);

  while (sensorDiff > 0.2 && sensorDiff < 6) {
    if (sensor_L_dis > sensor_R_dis) {
      md.setSpeeds(50, -50);
    }
    else if (sensor_R_dis > sensor_L_dis) {
      md.setSpeeds(-50, 50);
    }
    //delay(20);
    sensor_R_dis = ir_sense1();
    sensor_L_dis = ir_sense3();
    sensorDiff = abs(sensor_R_dis - sensor_L_dis);
  }
  md.setBrakes(400, 400);
}

// ensure that the right of the robot is straight
void alignRight() {
  double sensor_R_dis;
  double sensor_L_dis;

  double sensorDiff;

  sensor_R_dis = ir_sense4();
  sensor_L_dis = ir_sense5() - 0.4; //increase '0.2' if robot tilted right after alignment, dont forget below

  // if robot is too far from wall & there is insufficient wall to align against, don't align
  if ((sensor_R_dis > 11) || (sensor_L_dis > 11))
  {
    return;
  }

  sensorDiff = abs(sensor_R_dis - sensor_L_dis);

  while ((sensorDiff > 0.2) && (sensorDiff < 6)) {
    if (sensor_L_dis > sensor_R_dis) {
      md.setSpeeds(50, -50);
    }
    else if (sensor_R_dis > sensor_L_dis) {
      md.setSpeeds(-50, 50);
    }
    //delay(30);
    sensor_R_dis = ir_sense4();
    sensor_L_dis = ir_sense5() - 0.2;
    sensorDiff = abs(sensor_R_dis - sensor_L_dis);
  }
  //delay(20);
  md.setBrakes(400, 400);

}

void sendToRPi(char instruction, int arg)
{
  if (instruction == 'W')
  {
    Serial.print("W");
    Serial.print(arg);
  }
  else if (instruction == 'A')
  {
    Serial.print("A");
  }
  else if (instruction == 'D')
  {
    Serial.print("D");
  }
}


void shutdown()
{
  while (1);
}
