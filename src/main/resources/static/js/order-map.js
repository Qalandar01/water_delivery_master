function updateRouteSummary() {
    var key = String(currentCourier.id);
    var routeIds = selectedIdsPerCourier[key] || [];
    var summaryDiv = document.getElementById('routeSummary');

    if (!summaryDiv) return;

    // Filter out 'company' from route
    var orderIds = routeIds.filter(function(id) { return id !== 'company'; });

    if (orderIds.length === 0) {
        summaryDiv.innerHTML = '<span class="text-muted">Click orders on map to build route...</span>';
        return;
    }

    var summaryHtml = '<strong>Route: </strong>';
    summaryHtml += '<span class="badge badge-info">Company</span> → ';

    orderIds.forEach(function(orderId, index) {
        var order = orders.find(function(o) { return o.id === orderId; });
        if (order) {
            summaryHtml += '<span class="badge badge-primary" style="cursor:pointer;" onclick="highlightOrder(' + orderId + ')">';
            summaryHtml += (index + 1) + '. Order #' + orderId + '</span>';
            if (index < orderIds.length - 1) {
                summaryHtml += ' → ';
            }
        }
    });

    summaryHtml += '<br><small class="text-muted mt-2 d-block">Total: ' + orderIds.length + ' order(s) • Click order numbers to remove</small>';

    summaryDiv.innerHTML = summaryHtml;
}

function highlightOrder(orderId) {
    var pm = placemarks.find(function(p) { return p.orderId === orderId; });
    if (pm && pm.placemark) {
        map.setCenter(pm.placemark.geometry.getCoordinates(), 16);
        pm.placemark.balloon.open();
    }
}

function getOrderColor(order) {
    var key = String(currentCourier.id);
    var orderId = order.id;

    // ✅ GRAY: Check if assigned to another courier (database assigned)
    if (order.status === 'ASSIGNED' && order.courierId !== currentCourier.id) {
        var assignedToCurrentCourier = currentOrders.some(function(co) {
            return co.orderId === orderId && co.courierId === currentCourier.id;
        });
        if (!assignedToCurrentCourier) {
            return 'gray';
        }
    }

    // ✅ GRAY: Check if assigned to another courier (in planning)
    var assignedToOther = false;
    for (var courierId in selectedIdsPerCourier) {
        if (courierId !== key && selectedIdsPerCourier[courierId].indexOf(orderId) !== -1) {
            assignedToOther = true;
            break;
        }
    }

    if (assignedToOther) {
        return 'gray';
    }

    // ✅ PURPLE: In current courier's planned route (not yet saved)
    if (selectedIdsPerCourier[key] && selectedIdsPerCourier[key].indexOf(orderId) !== -1) {
        return 'violet';
    }

    // ✅ Date-based colors for available orders
    var orderDate = order.day;
    var today = new Date().toISOString().split('T')[0];
    var yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday = yesterday.toISOString().split('T')[0];

    if (orderDate === today) return 'green';
    if (orderDate === yesterday) return 'orange';
    return 'red';
}

function buildHint(order) {
    var productsHtml = '';
    if (order.products && order.products.length > 0) {
        productsHtml = order.products.map(function(p) {
            return '<li><span>' + p.name + '</span> x' + p.amount + '</li>';
        }).join('');
    }

    return '<div style="font-family:Arial;font-size:12px;padding:8px;max-width:250px;">' +
        '<div style="font-weight:bold;margin-bottom:4px;">🆔 Order #' + order.id + '</div>' +
        '<div style="margin-bottom:2px;">📅 ' + formatDate(order.date) + '</div>' +
        '<div style="margin-bottom:6px;">📞 ' + order.phone + '</div>' +
        '<ul style="margin:0;padding-left:20px;">' + productsHtml + '</ul>' +
        '</div>';
}

function buildUnassignButton(orderId) {
    return '<form action="/operator/orders/unassign" method="post" class="unassign-form" style="margin-top:8px;">' +
        '<input type="hidden" name="orderId" value="' + orderId + '">' +
        '<button type="submit" class="btn btn-danger btn-sm">🚚 Release Order</button>' +
        '</form>';
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    var d = new Date(dateStr);
    return d.toLocaleString();
}

function onChangeAll(checkbox) {
    var showAll = checkbox.checked;

    placemarks.forEach(function(pm) {
        try {
            map.geoObjects.remove(pm.placemark);
            if (pm.circle) map.geoObjects.remove(pm.circle);
        } catch(e) {}
    });

    if (showAll) {
        placemarks.forEach(function(pm) {
            if (pm.circle) map.geoObjects.add(pm.circle);
            map.geoObjects.add(pm.placemark);
        });
    } else {
        var key = String(currentCourier.id);

        placemarks.forEach(function(pm) {
            var order = orders.find(function(o) {
                return o.id === pm.orderId;
            });

            if (!order) return;

            var selectedByCurrent = selectedIdsPerCourier[key].indexOf(order.id) !== -1;
            var notAssignedToAnyone = true;

            for (var courierId in selectedIdsPerCourier) {
                if (selectedIdsPerCourier[courierId].indexOf(order.id) !== -1) {
                    notAssignedToAnyone = false;
                    break;
                }
            }

            if (selectedByCurrent || notAssignedToAnyone) {
                if (order.status === 'ASSIGNED') {
                    var assignedToOther = currentOrders.some(function(co) {
                        return co.orderId === order.id && co.courierId !== currentCourier.id;
                    });
                    if (assignedToOther) return;
                }
                if (pm.circle) map.geoObjects.add(pm.circle);
                map.geoObjects.add(pm.placemark);
            }
        });
    }
}

function handleSearchOrders(input) {
    var searchValue = input.value.trim();
    resetPlacemarkStyles();

    if (!searchValue) return;

    var foundOrders = orders.filter(function(order) {
        return order.phone && order.phone.includes(searchValue);
    });

    if (foundOrders.length > 0) {
        var orderId = foundOrders[0].id;
        var pm = placemarks.find(function(p) {
            return p.orderId === orderId;
        });

        if (pm && pm.placemark) {
            var coords = pm.placemark.geometry.getCoordinates();
            map.setCenter(coords, 17); // Zoom in more for better visibility
            pm.placemark.options.set({
                preset: 'islands#redCircleDotIcon',
                iconImageSize: [50, 50],
                iconImageOffset: [-25, -50]
            });

            // Pulse animation effect
            setTimeout(function() {
                pm.placemark.options.set({
                    iconImageSize: [45, 45],
                    iconImageOffset: [-22, -45]
                });
            }, 200);
        }
    } else {
        showAlert('No orders found with phone: ' + searchValue);
    }
}

function resetPlacemarkStyles() {
    placemarks.forEach(function(pm) {
        var key = String(currentCourier.id);
        var isInCurrentRoute = selectedIdsPerCourier[key] &&
            selectedIdsPerCourier[key].indexOf(pm.orderId) !== -1;

        var iconPreset = 'islands#circleIcon';
        var iconSize = isInCurrentRoute ? [45, 45] : [35, 35];
        var iconOffset = isInCurrentRoute ? [-22, -45] : [-17, -35];

        pm.placemark.options.set({
            preset: iconPreset,
            iconImageSize: iconSize,
            iconImageOffset: iconOffset
        });
    });
}

function showAlert(message) {
    var alertDiv = document.getElementById('alert-message');
    var alertText = document.getElementById('alert-text');
    alertText.textContent = message;
    alertDiv.classList.remove('hide');

    setTimeout(function() {
        hideAlert();
    }, 5000);
}

function hideAlert() {
    document.getElementById('alert-message').classList.add('hide');
}
function initMap() {
    map = new ymaps.Map('map', {
        center: companyCoords,
        zoom: 13,
        controls: ['zoomControl', 'fullscreenControl']
    });

    var companyPlacemark = new ymaps.Placemark(companyCoords, {
        hintContent: 'Company Location - Click to reset route'
    }, {
        preset: 'islands#blueHomeIcon',
        iconColor: 'blue',
        cursor: 'pointer'
    });

    map.geoObjects.add(companyPlacemark);

    // ✅ COMPANY CLICK: Reset/Undraw entire route
    companyPlacemark.events.add('click', function() {
        var assignedForCurrent = currentOrders.filter(function(item) {
            return item.courierId === currentCourier.id;
        });

        var key = String(currentCourier.id);

        // If courier has assigned orders, reset to last assigned position
        if (assignedForCurrent.length > 0) {
            var lastOrder = assignedForCurrent[assignedForCurrent.length - 1];
            selectedIdsPerCourier[key] = ['company'];
            selectedPointsPerCourier[key] = [
                [lastOrder.location.latitude, lastOrder.location.longitude]
            ];
            showAlert('🔄 Route reset to last assigned order');
        } else {
            // No assigned orders, reset to company
            selectedIdsPerCourier[key] = ['company'];
            selectedPointsPerCourier[key] = [companyCoords];
            showAlert('🔄 Route completely reset');
        }

        reRenderMap();
    });

    renderOrders();
    renderOtherCouriersPolylines();
    renderCurrentCourierPolyline();
    renderAssignedOrdersLine();
}

function renderOrders() {
    // Remove existing order placemarks
    placemarks.forEach(function(p) {
        try {
            map.geoObjects.remove(p.placemark);
        } catch(e) {
            console.error('Error removing placemark:', e);
        }
    });
    placemarks = [];

    orders.forEach(function(order) {
        var lat = order.location && order.location.latitude;
        var lon = order.location && order.location.longitude;

        if (!lat || !lon) {
            console.warn('Order missing location:', order.id);
            return;
        }

        var color = getOrderColor(order);
        var hint = buildHint(order);

        if (order.status === 'ASSIGNED') {
            hint += buildUnassignButton(order.id);
        }

        // ✅ Check if order is in current courier's planned route
        var key = String(currentCourier.id);
        var isInCurrentRoute = selectedIdsPerCourier[key] &&
            selectedIdsPerCourier[key].indexOf(order.id) !== -1;

        // ✅ Choose icon preset based on order state
        var iconPreset = 'islands#circleIcon'; // Default: larger circle

        if (order.status === 'ASSIGNED') {
            iconPreset = 'islands#circleDotIcon';
        } else if (isInCurrentRoute) {
            iconPreset = 'islands#circleIcon';
        }

        // ✅ BIGGER ICON OPTIONS for easier clicking
        var placemarkOptions = {
            preset: iconPreset,
            iconColor: color,
            cursor: 'pointer',
            hideIconOnBalloonOpen: false,
            openBalloonOnClick: false,
            zIndex: isInCurrentRoute ? 2000 : 1000,
            zIndexHover: 3000
        };

        // ✅ Make icon bigger based on importance
        if (isInCurrentRoute) {
            // Selected orders get bigger icons
            placemarkOptions.iconImageSize = [45, 45];
            placemarkOptions.iconImageOffset = [-22, -45];
        } else {
            // Regular orders get medium icons
            placemarkOptions.iconImageSize = [35, 35];
            placemarkOptions.iconImageOffset = [-17, -35];
        }

        var placemark = new ymaps.Placemark([lat, lon], {
            hintContent: hint,
            balloonContent: hint
        }, placemarkOptions);

        // ✅ Add larger invisible circle for better click detection
        var clickableCircle = new ymaps.Circle(
            [[lat, lon], 30], // 30 meter radius
            {},
            {
                fillOpacity: 0,
                strokeOpacity: 0,
                cursor: 'pointer',
                zIndex: 500
            }
        );

        // Both placemark and circle trigger the same click handler
        placemark.events.add('click', function() {
            handlePlacemarkClick(order, lat, lon);
        });

        clickableCircle.events.add('click', function() {
            handlePlacemarkClick(order, lat, lon);
        });

        placemarks.push({
            placemark: placemark,
            circle: clickableCircle,
            orderId: order.id
        });

        map.geoObjects.add(clickableCircle); // Add invisible circle first
        map.geoObjects.add(placemark); // Add visible marker on top
    });
}

function handlePlacemarkClick(order, lat, lon) {
    var key = String(currentCourier.id);
    var orderId = order.id;
    var point = [lat, lon];

    // Handle ASSIGNED orders (already saved to database)
    if (order.status === 'ASSIGNED') {
        var assignedOrders = currentOrders.filter(function(item) {
            return item.courierId === currentCourier.id;
        });

        if (assignedOrders.length > 0) {
            var lastOrder = assignedOrders[assignedOrders.length - 1];
            if (lastOrder.orderId === order.id) {
                selectedIdsPerCourier[key] = ['company'];
                selectedPointsPerCourier[key] = [
                    [lastOrder.location.latitude, lastOrder.location.longitude]
                ];
                reRenderMap();
                showAlert('Start planning route from the last assigned order');
            } else {
                showAlert('Can only modify route from the last assigned order');
            }
        }
        return;
    }

    // Check if order already assigned to another courier (in planning)
    var assignedToOther = false;
    for (var courierId in selectedIdsPerCourier) {
        if (courierId !== key && selectedIdsPerCourier[courierId].indexOf(orderId) !== -1) {
            assignedToOther = true;
            break;
        }
    }

    if (assignedToOther) {
        showAlert('This order is already assigned to another courier');
        return;
    }

    // Check if order is already in current courier's route
    var existingIndex = selectedIdsPerCourier[key].indexOf(orderId);

    if (existingIndex !== -1) {
        // ✅ UNDRAW: Clicking an already selected order removes it and all after it
        console.log('🔙 Undrawing from order #' + orderId + ' (index: ' + existingIndex + ')');

        // Keep only points up to (and including) the clicked order
        selectedPointsPerCourier[key] = selectedPointsPerCourier[key].slice(0, existingIndex + 1);
        selectedIdsPerCourier[key] = selectedIdsPerCourier[key].slice(0, existingIndex + 1);

        showAlert('Route trimmed to Order #' + orderId);
    } else {
        // ✅ DRAW: Add new order to route
        console.log('➕ Adding order #' + orderId + ' to route');
        selectedPointsPerCourier[key].push(point);
        selectedIdsPerCourier[key].push(orderId);

        var routeLength = selectedIdsPerCourier[key].length - 1; // -1 for 'company'
        showAlert('Added Order #' + orderId + ' (Total: ' + routeLength + ' orders)');
    }

    reRenderMap();
}

function renderOtherCouriersPolylines() {
    couriers.forEach(function(courier) {
        if (courier.id === currentCourier.id) return;

        var locations = [companyCoords];
        var courierId = String(courier.id);

        // Add assigned orders
        currentOrders.filter(function(item) {
            return item.courierId === courier.id;
        }).forEach(function(item) {
            locations.push([item.location.latitude, item.location.longitude]);
        });

        // Add selected points
        if (selectedPointsPerCourier[courierId]) {
            locations = locations.concat(selectedPointsPerCourier[courierId]);
        }

        if (locations.length >= 2) {
            var polyline = new ymaps.Polyline(locations, {
                hintContent: 'Route for ' + courier.firstName
            }, {
                strokeColor: 'rgba(128,128,128,0.5)',
                strokeWidth: 5,
                strokeOpacity: 0.7
            });

            polyline.events.add('click', function() {
                window.location.href = '?courier=' + courier.id;
            });

            map.geoObjects.add(polyline);
        }
    });
}

function renderCurrentCourierPolyline() {
    var key = String(currentCourier.id);
    var points = selectedPointsPerCourier[key];

    if (points && points.length >= 2) {
        var polyline = new ymaps.Polyline(points, {
            hintContent: 'Planned route for ' + currentCourier.firstName
        }, {
            strokeColor: '#0000FF',
            strokeWidth: 7,
            strokeOpacity: 0.8
        });
        map.geoObjects.add(polyline);
    }
}

function renderAssignedOrdersLine() {
    var locations = [companyCoords];

    var assignedOrders = currentOrders.filter(function(item) {
        return item.courierId === currentCourier.id;
    });

    assignedOrders.forEach(function(order) {
        locations.push([order.location.latitude, order.location.longitude]);
    });

    if (locations.length >= 2) {
        var polyline = new ymaps.Polyline(locations, {
            hintContent: 'Assigned orders for ' + currentCourier.firstName
        }, {
            strokeColor: '#00FF00',
            strokeWidth: 7,
            strokeOpacity: 0.8
        });
        map.geoObjects.add(polyline);
    }
}

function reRenderMap() {
    map.geoObjects.removeAll();

    // Re-add company marker with click handler
    var companyPlacemark = new ymaps.Placemark(companyCoords, {
        hintContent: 'Company Location - Click to reset route'
    }, {
        preset: 'islands#blueHomeIcon',
        iconColor: 'blue',
        cursor: 'pointer'
    });

    companyPlacemark.events.add('click', function() {
        var assignedForCurrent = currentOrders.filter(function(item) {
            return item.courierId === currentCourier.id;
        });

        var key = String(currentCourier.id);

        if (assignedForCurrent.length > 0) {
            var lastOrder = assignedForCurrent[assignedForCurrent.length - 1];
            selectedIdsPerCourier[key] = ['company'];
            selectedPointsPerCourier[key] = [
                [lastOrder.location.latitude, lastOrder.location.longitude]
            ];
            showAlert('🔄 Route reset to last assigned order');
        } else {
            selectedIdsPerCourier[key] = ['company'];
            selectedPointsPerCourier[key] = [companyCoords];
            showAlert('🔄 Route completely reset');
        }

        reRenderMap();
    });

    map.geoObjects.add(companyPlacemark);

    renderOrders();
    renderOtherCouriersPolylines();
    renderCurrentCourierPolyline();
    renderAssignedOrdersLine();

    // ✅ Show route sequence numbers
    renderRouteNumbers();
}

/**
 * ✅ NEW: Show numbers on orders in the planned route
 */
function renderRouteNumbers() {
    var key = String(currentCourier.id);
    var routeIds = selectedIdsPerCourier[key] || [];

    routeIds.forEach(function(orderId, index) {
        if (orderId === 'company') return; // Skip company

        var order = orders.find(function(o) { return o.id === orderId; });
        if (!order || !order.location) return;

        var sequenceNumber = index; // 0-based, but company is 0, so first order is 1

        var numberPlacemark = new ymaps.Placemark(
            [order.location.latitude, order.location.longitude],
            {
                iconContent: String(sequenceNumber)
            },
            {
                preset: 'islands#blueStretchyIcon',
                cursor: 'pointer'
            }
        );

        numberPlacemark.events.add('click', function() {
            handlePlacemarkClick(order, order.location.latitude, order.location.longitude);
        });

        map.geoObjects.add(numberPlacemark);
    });

    // Update route summary panel
    updateRouteSummary();
}
function initializeData() {
    // Initialize for all couriers
    couriers.forEach(function(courier) {
        var courierId = String(courier.id);

        if (!selectedIdsPerCourier[courierId]) {
            selectedIdsPerCourier[courierId] = ['company'];
        }

        if (!selectedPointsPerCourier[courierId]) {
            var assignedOrders = currentOrders.filter(function(item) {
                return item.courierId === courier.id;
            });

            if (assignedOrders.length > 0) {
                var lastOrder = assignedOrders[assignedOrders.length - 1];
                selectedPointsPerCourier[courierId] = [
                    [lastOrder.location.latitude, lastOrder.location.longitude]
                ];
            } else {
                selectedPointsPerCourier[courierId] = [companyCoords];
            }
        }
    });
}
