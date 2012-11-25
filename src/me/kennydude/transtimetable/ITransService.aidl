package me.kennydude.transtimetable;

interface ITransService{
	// Note: This uses JSON for simplicity
	
	// Not required, but you might as well have it! :D
	String getNearbyStations( double lat, double lon );
	String getMatchingStations( String query );
	
	// You need these per station, this is why your station ID is required
	// for national rail we might use DLA or UNI, see Station
	String getStationDepartures( String stationID );
	String getStationArrivals( String stationID );
	
	// More information on the train, see TransitItem
	String getTransitItemDetails( String id );
	
	String getStationDetails( String id );
	String getTravelUpdates( String id );
}