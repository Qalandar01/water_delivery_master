/**
 * orderMapInit.js
 * Main initialization and map setup
 */

// ============================================
// MAP INITIALIZATION
// ============================================

/**
 * Initialize the map
 */
function initMap() {
    OrderMapState.map = new ymaps.Map('map', {
        center: OrderMapState.companyCoords,
        zoom: OrderMapConfig.map.defaultZoom,
        controls: OrderMapConfig.map.controls
    });

    // Add company marker
    var companyPlacemark = new ymaps.Placemark(OrderMapState.companyCoords, {
        hintContent: 'Company Location - Click to reset route'
    }, {
        preset: 'islands#blueHomeIcon',
        iconColor: 'blue',
        cursor: 'pointer'
    });

    OrderMapState.map.geoObjects.add(companyPlacemark);
    companyPlacemark.events.add('click', handleCompanyClick);

    // Render all map elements
    renderOrders();
    renderOtherCouriersPolylines();
    renderCurrentCourierPolyline();
    renderAssignedOrdersLine();
    renderRouteNumbers();

    console.log('✅ Map initialized successfully');
}

// ============================================
// MAIN INITIALIZATION FUNCTION
// ============================================

/**
 * Main initialization - called when Yandex Maps API is ready
 */
function initializeOrderMap() {
    // Initialize courier data
    initializeCourierData();

    // Initialize map
    initMap();

    // Setup form handlers
    setupFormHandlers();

    // Log debug info
    logMapDebugInfo();
}

/**
 * Setup form event handlers
 */
function setupFormHandlers() {
    // Assignment form submission
    var assignForm = document.getElementById('assignForm');
    if (assignForm) {
        assignForm.addEventListener('submit', handleAssignFormSubmit);
    }

    // Unassign form submissions (delegated)
    document.addEventListener('submit', function(e) {
        if (e.target && e.target.classList && e.target.classList.contains('unassign-form')) {
            // Re-initialize data on unassign
            setTimeout(function() {
                initializeCourierData();
            }, 100);
        }
    });

    console.log('✅ Form handlers setup complete');
}

// ============================================
// ENTRY POINT
// ============================================

/**
 * Entry point - wait for Yandex Maps API to be ready
 */
ymaps.ready(initializeOrderMap);