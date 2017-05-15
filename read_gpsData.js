var com = require("serialport"); 
var nmea = require("nmea-0183");

var port = "/dev/ttyMFD1";  // Get in serial 
var baudrate = 9600;  /// Check with product 
var line_end = '\r\n';

var serialPort = new com.SerialPort(port, { 
    baudrate: baudrate,
    parser: com.parsers.readline(line_end) 
});

serialPort.on('open', function(err) { 
    if(err) {
        console.log('serialPort: error'); 
    } else { 
        console.log ('serialPort: open');
        SerialPort.on('data', function(data) { 
            try {
                var gps = nmea.parse(data);
                if(gps['id'] === 'GPRMC') {
                    console.log(gps);
                    put_item(gps['time'], gps['date'], gps['longitude'], gps['latitude']);
                }
                                // console . log(data); 
                console.log(gps); 
            } catch(e) { 
                console.log(e);                  
            }    
        });                               
    } 
});

function put_item(time, date, longitude, latitude) {
    var params = {
        TableName:'gps',
        Item:{
            "time": time,
            'date': date,
            'longitude': longitude,
            'latitude': latitude
        }
    };

    console.log("Adding a new item...");
    docClient.put(params, function(err, data) {
        if (err) {
            console.error("Unable to add item. Error JSON:", JSON.stringify(err, null, 2));
        } else {
            console.log("Added item:", JSON.stringify(data, null, 2));
        }
    });
}