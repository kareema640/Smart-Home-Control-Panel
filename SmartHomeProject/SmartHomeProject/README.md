# Smart Home Control Panel — Project 8
**Category:** IoT / Embedded Systems  
**IDE:** NetBeans 12+ | **Language:** Java 11+ with JavaFX 17+

---

## Project Structure

```
SmartHomeProject/
├── src/smarthome/
│   ├── SmartHomeApp.java              ← Main entry point
│   ├── style.css                      ← All UI styling
│   ├── model/
│   │   ├── SmartDevice.java           ← Abstract base class
│   │   ├── MQTTPublisher.java         ← Interface
│   │   ├── Alertable.java             ← Interface
│   │   ├── Room.java
│   │   └── AlertEntry.java
│   │   └── devices/
│   │       ├── Lighting.java          ← Abstract
│   │       ├── SmartBulb.java
│   │       ├── RGBStrip.java
│   │       ├── Climate.java           ← Abstract
│   │       ├── Thermostat.java
│   │       ├── AirPurifier.java
│   │       ├── Security.java          ← Abstract
│   │       ├── Camera.java
│   │       └── MotionSensor.java
│   ├── controller/
│   │   └── HomeController.java
│   └── view/
│       ├── MainView.java
│       ├── BaseRoomView.java
│       ├── LivingRoomView.java        ← Desktop-1
│       ├── KitchenView.java           ← Desktop-2
│       ├── BedroomView.java           ← Desktop-3
│       └── ToggleSwitch.java          ← Custom control
├── nbproject/
│   ├── project.xml
│   └── project.properties
├── build.xml
└── manifest.mf
```

---

## Class Hierarchy (as required)

```
SmartDevice (abstract)
├── Lighting (abstract)
│   ├── SmartBulb
│   └── RGBStrip
├── Climate (abstract)
│   ├── Thermostat
│   └── AirPurifier
└── Security (abstract)
    ├── Camera
    └── MotionSensor

Interfaces: MQTTPublisher, Alertable
```

---

## Setup in NetBeans

### Step 1 – Download JavaFX SDK
1. Go to https://gluonhq.com/products/javafx/
2. Download **JavaFX 17 LTS** (or 21) for your OS
3. Extract it somewhere, e.g. `C:\javafx-sdk-17\` or `/home/user/javafx-sdk-17/`

### Step 2 – Open Project
1. Open NetBeans
2. **File → Open Project** → select the `SmartHomeProject` folder
3. NetBeans will detect it as a Java project

### Step 3 – Add JavaFX Library
1. Right-click the project → **Properties**
2. Go to **Libraries** → **Compile** tab → **Add Library...**
3. If "JavaFX 17" library doesn't exist:
   - Click **Manage Platforms / Libraries**
   - **New Library** → name it `JavaFX17`
   - Add all JARs from `javafx-sdk-17/lib/`
4. Add the `JavaFX17` library to your project

### Step 4 – Set VM Options
1. Right-click project → **Properties → Run**
2. In **VM Options** field, enter (adjust path to your JavaFX SDK):

**Windows:**
```
--module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

**Mac/Linux:**
```
--module-path /home/user/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

3. Set **Main Class** to: `smarthome.SmartHomeApp`

### Step 5 – Run
Press **F6** or click the green Run button ▶

---

## Features Implemented

| Feature | Status |
|---------|--------|
| 3-room dashboard (Living, Kitchen, Bedroom) | ✅ |
| Real-time sensor simulation (TimerTask) | ✅ |
| JavaFX Property bindings (live updates) | ✅ |
| Custom iOS-style ToggleSwitch | ✅ |
| Temperature dial with arc animation | ✅ |
| Air purifier purity gauge | ✅ |
| RGB light color toggles | ✅ |
| Motion sensor with alert feed | ✅ |
| Security ID login card | ✅ |
| Brightness slider with binding | ✅ |
| Alert history ListView | ✅ |
| MQTTPublisher interface (simulated) | ✅ |
| Alertable interface | ✅ |
| Observer pattern via JavaFX Properties | ✅ |
| CSS theming | ✅ |

---

## Troubleshooting

**Error: "JavaFX runtime components are missing"**  
→ VM options are not set correctly. See Step 4 above.

**Error: "Cannot find symbol" on var keyword**  
→ Make sure Java source level is set to 11+. Right-click project → Properties → Sources → Source/Binary Format → **11**

**CSS not loading**  
→ Make sure `style.css` is inside the `src/smarthome/` folder (same package as `SmartHomeApp.java`)
