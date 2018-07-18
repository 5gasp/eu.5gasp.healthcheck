# eu.5ginfire.healthcheck
Healthcheck service for 5GinFIRE Infrastructure
<!-- TITLE: The 5GinFIRE Healthcheck service (HCS) -->
<!-- SUBTITLE: Introduction and Usage of the 5GinFIRE Healthcheck service -->

# Introduction and Usage of the 5GinFIRE Healthcheck service (HCS)
The Healthcheck Service (HCS) is a simple service that displays the status of various services, VIMs, connectivity, components and processes of the 5GinFIRE infrastructure. 
It is deployed at http://status.5ginfire.eu/

![Hcs Snapshot](https://github.com/5GinFIRE/wiki/blob/master/uploads/hcs/hcs-snapshot.png "Hcs Snapshot")

## How it works

**Components** are polled (**ACTIVE** mode) or report their status (**PASSIVE** mode). 
If the HCS fails to learn the status of a **Component** within a defined  **Threshold** then the component is marked as FAIL/DOWN.

## Components

We have defined the following Component Types:
* 	SERVICE : a generic service, e.g. PORTAL,OSM
* 	PROCESS: an automated process that is successful or not. E.g. PING_PING_INSTANTIATION_TEST executed by jenkins every night
* 	VIM: a facility,e.g. BRISTOL
* 	CONNECTIVITY: a connection between components. eg PORTAL-OSM, OSM-ITAV, OSM-BRISTOL, etc

## Modes
There are two modes ACTIVE and PASSIVE.

### ACTIVE mode

During active mode the HCS polls the **Component** via a URL. If the URL returns 200 OK then the **Component** is UP. 
A constraint here is that **the HCS must have a connectivity with the target component**.

Example the HCS will poll the PORTAL component. 
Component name = PORTAL
mode: ACTIVE,
checkURL": https://portal.5ginfire.eu/index.html
failoverThreshold: 600

In this example the HCS will check regularly the URL https://5ginfire.portal.eu. If it fails to get a 200 OK will try again in a few minutes. If within 10 minutes (600 seconds) fails to get a 200 OK then the service will be marked as DOWN. An Issue will be raised automatically.


### PASSIVE mode

The PASSIVE mode can be used by components that cannot accept connections from the HCS but have internet connection (e.g. a VIM). 
The component reports that is alive through a GET request to the HCS. The URL format is:

```text
[hcserviceurl]/admin/components/{*componentname*}/{*apikey*}
```

*componentname* is a Unique name of the component, e.g. PORTAL, OSM, BRISTOL
*apikey* is a unique string for each component. (apikeys will be given by the HCS admins)

Again if the service will not report its status within *failoverThreshold*  the service will be marked as DOWN. An Issue will be raised automatically for this component via Bugzilla to respective owners of the Component if previously was UP.

**Example 1: VIM is alive**
We have defined that failoverThreshold for BRISTOL VIM is 10 minutes. 
Then BRISTOL needs to report that it is alive every e.g. 5 minutes ( for example in a cron with a script ):

```text
wget   http://status.5ginfire.eu/hcs/services/api/admin/components/BRISTOL/8756118f-66bf-1234-a409-22e37f89c0c0
```

If BRISTOL fails to send the above request within 10 minutes an issue will be raised in Bugzilla for BRISTOL and will marked as down, if previously was UP.

**Example 2: PORTAL-OSM link is alive**
Portal needs to report that the link to OSM host machine is alive (note: not the OSM service). 
Portal has a VPN connection with OSM at 5TONIC.
If PING is succesful from Portal to OSM host machine then we will report that the PORTAL-OSM link is UP.
We create a file : pingOSMlink.sh

```text
ping -c1 10.4.16.15
  if [ $? -eq 0 ]
  then
    wget http://status.5ginfire.eu/hcs/services/api/admin/components/PORTAL-OSM/12345678-1234-1234-e830-22e37fa6a000
    exit 0
  fi
```

then in crontab, to check every minute:

```text
*/1 * * * * /home/ubuntu/pingOSMlink.sh
```


## Component status

A Component can be in the following states:	
* UP: is alive
* DOWN: is not alive
* PASS: used in PROCESS type. The process passes test within threshold
* FAIL: used in PROCESS type. The process failed test within threshold
				

## Component description model

Each component has a defined model internally in HCS. For example, PORTAL Web:


        "longitude": "21.234456",
        "name": "PORTAL",
        "locationName": "Patras,GR",
        "mode": "ACTIVE",
        "apikey": "*******",
        "checkURL": "https://5ginfire.portal.eu",
        "type": "SERVICE",
        "latitude": "38.220220",
        "onIssueNotificationProduct": "Platform",
        "onIssueNotificationComponent": "Portal",
        "description": "Portal Component",
        "failoverThreshold": 600
				

onIssueNotificationProduct: On ERROR the Product to report in Bugzilla
onIssueNotificationComponent On ERROR the Component to report in Bugzilla
failoverThreshold: In seconds
checkURL: The URL to call if MODE=ACTIVE


# Registering to the 5GinFIRE HCS,  providing Name and APIKEY

Admins of HCS will create the component and give a Unique Name and APIKEY. Will also agree the mode, the Bugzilla product, component, etc.
If you would like to include your component in HCS please raise an issue in Bugzilla, 5GinFIRE Operations/Operations Support (https://portal.5ginfire.eu/bugzilla/enter_bug.cgi?product=5GinFIRE%20Operations),  
We will provide you with an APIKEY

# Source code

You can download or contribute to https://github.com/5GinFIRE/eu.5ginfire.healthcheck



