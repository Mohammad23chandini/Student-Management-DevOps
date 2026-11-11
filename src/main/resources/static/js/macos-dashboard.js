/**
 * macOS Dashboard Interactive Controller
 * Handles Navigation, Table Filtering, Multi-Selection, Sorting, Pagination,
 * Modals, Real-time Validation, Charts, and System Settings.
 */

document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initTheme();
    initNavigation();
    initSidebar();
    initWindowControls();
    initStudentTable();
    initModals();
    initFormValidation();
    initCharts();
    initToasts();
});

/* ==========================================================================
   1. Live Clock
   ========================================================================== */
function initClock() {
    const clockEl = document.getElementById('system-clock');
    if (!clockEl) return;

    function update() {
        const now = new Date();
        const options = { weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
        clockEl.textContent = now.toLocaleDateString('en-US', options);
    }
    update();
    setInterval(update, 1000 * 30);
}

/* ==========================================================================
   2. Theme Switcher (macOS Light / Dark Mode)
   ========================================================================== */
function initTheme() {
    const savedTheme = localStorage.getItem('macos-theme') || 'light';
    applyTheme(savedTheme);

    const toggleBtn = document.getElementById('theme-toggle-btn');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const current = document.documentElement.getAttribute('data-theme') || 'light';
            const next = current === 'dark' ? 'light' : 'dark';
            applyTheme(next);
            localStorage.setItem('macos-theme', next);
        });
    }

    const themeSelect = document.getElementById('settings-theme-select');
    if (themeSelect) {
        themeSelect.value = savedTheme;
        themeSelect.addEventListener('change', (e) => {
            applyTheme(e.target.value);
            localStorage.setItem('macos-theme', e.target.value);
        });
    }
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    const sunIcon = document.getElementById('sun-icon');
    const moonIcon = document.getElementById('moon-icon');
    if (sunIcon && moonIcon) {
        if (theme === 'dark') {
            sunIcon.style.display = 'block';
            moonIcon.style.display = 'none';
        } else {
            sunIcon.style.display = 'none';
            moonIcon.style.display = 'block';
        }
    }
}

/* ==========================================================================
   3. Window Controls (Traffic Lights)
   ========================================================================== */
function initWindowControls() {
    const windowEl = document.querySelector('.macos-window');
    const zoomBtn = document.getElementById('traffic-zoom');
    const minBtn = document.getElementById('traffic-minimize');
    const closeBtn = document.getElementById('traffic-close');

    if (zoomBtn && windowEl) {
        zoomBtn.addEventListener('click', () => {
            windowEl.classList.toggle('fullscreen');
        });
    }

    if (minBtn && windowEl) {
        minBtn.addEventListener('click', () => {
            windowEl.style.transform = 'scale(0.96)';
            windowEl.style.opacity = '0.7';
            setTimeout(() => {
                windowEl.style.transform = 'scale(1)';
                windowEl.style.opacity = '1';
            }, 300);
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            showToast('info', 'Window Control', 'macOS dashboard is active.');
        });
    }
}

/* ==========================================================================
   4. Sidebar Toggle & Responsive
   ========================================================================== */
function initSidebar() {
    const sidebar = document.querySelector('.macos-sidebar');
    const collapseBtn = document.getElementById('sidebar-collapse-btn');

    if (collapseBtn && sidebar) {
        collapseBtn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
}

/* ==========================================================================
   5. Tab Navigation
   ========================================================================== */
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const viewSections = document.querySelectorAll('.view-section');
    const headerTitle = document.getElementById('content-header-title');
    const headerDesc = document.getElementById('content-header-desc');

    const titles = {
        overview: { title: 'Dashboard Overview', desc: 'Real-time academic metrics & system statistics' },
        students: { title: 'Student Management', desc: 'Manage directory, enrollment records and department profiles' },
        analytics: { title: 'Analytics & Insights', desc: 'Departmental distribution and student enrollment metrics' },
        reports: { title: 'Reports & Data Export', desc: 'Generate CSV archives and printable student rosters' },
        settings: { title: 'System Settings', desc: 'Application configuration, database connection and preferences' }
    };

    function switchSection(targetKey) {
        if (!targetKey) targetKey = 'overview';

        navItems.forEach(item => {
            const key = item.getAttribute('data-section');
            if (key === targetKey) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        viewSections.forEach(section => {
            if (section.id === 'section-' + targetKey) {
                section.classList.add('active');
            } else {
                section.classList.remove('active');
            }
        });

        if (titles[targetKey] && headerTitle && headerDesc) {
            headerTitle.textContent = titles[targetKey].title;
            headerDesc.textContent = titles[targetKey].desc;
        }

        // Trigger chart resize if navigating to analytics or overview
        if (targetKey === 'overview' || targetKey === 'analytics') {
            setTimeout(resizeCharts, 50);
        }
    }

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            const key = item.getAttribute('data-section');
            if (key === 'add-modal') {
                e.preventDefault();
                openModal('modal-add-student');
                return;
            }
            if (key) {
                e.preventDefault();
                switchSection(key);
                history.pushState(null, '', '#' + key);
            }
        });
    });

    // Handle hash on initial load
    const hash = window.location.hash.replace('#', '');
    if (hash && document.getElementById('section-' + hash)) {
        switchSection(hash);
    } else {
        const initialActive = document.querySelector('.view-section.active');
        if (initialActive) {
            const initialKey = initialActive.id.replace('section-', '');
            if (titles[initialKey] && headerTitle && headerDesc) {
                headerTitle.textContent = titles[initialKey].title;
                headerDesc.textContent = titles[initialKey].desc;
            }
        }
    }

    // Quick action triggers
    document.querySelectorAll('[data-switch-to]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const target = btn.getAttribute('data-switch-to');
            switchSection(target);
            history.pushState(null, '', '#' + target);
        });
    });
}

/* ==========================================================================
   6. Student Data Table (Search, Filter, Sort, Pagination, Multi-Select)
   ========================================================================== */
function initStudentTable() {
    const table = document.getElementById('students-data-table');
    if (!table) return;

    const tbody = table.querySelector('tbody');
    const allRows = Array.from(tbody.querySelectorAll('tr.student-row'));

    const searchInput = document.getElementById('table-search-input');
    const deptFilter = document.getElementById('table-dept-filter');
    const sortSelect = document.getElementById('table-sort-select');
    const pageSizeSelect = document.getElementById('page-size-select');
    const paginationControls = document.getElementById('pagination-controls');
    const paginationInfo = document.getElementById('pagination-info');

    const selectAllCheckbox = document.getElementById('select-all-students');
    const bulkBar = document.getElementById('bulk-actions-bar');
    const bulkCountEl = document.getElementById('bulk-selected-count');
    const bulkDeleteForm = document.getElementById('bulk-delete-form');
    const bulkDeleteInputContainer = document.getElementById('bulk-delete-ids-container');

    let currentPage = 1;
    let pageSize = 10;
    let filteredRows = [...allRows];

    // Pastel Avatar Palette for macOS look
    const avatarGradients = [
        'linear-gradient(135deg, #0071e3, #47a6ff)',
        'linear-gradient(135deg, #af52de, #5856d6)',
        'linear-gradient(135deg, #34c759, #30b0c7)',
        'linear-gradient(135deg, #ff9500, #ff3b30)',
        'linear-gradient(135deg, #5856d6, #af52de)',
        'linear-gradient(135deg, #30b0c7, #0071e3)'
    ];

    allRows.forEach((row, idx) => {
        const avatar = row.querySelector('.student-avatar');
        if (avatar) {
            avatar.style.background = avatarGradients[idx % avatarGradients.length];
        }
    });

    function applyFilters() {
        const query = (searchInput ? searchInput.value : '').toLowerCase().trim();
        const selectedDept = (deptFilter ? deptFilter.value : 'ALL').toUpperCase();

        filteredRows = allRows.filter(row => {
            const name = (row.getAttribute('data-name') || '').toLowerCase();
            const email = (row.getAttribute('data-email') || '').toLowerCase();
            const dept = (row.getAttribute('data-dept') || '').toUpperCase();

            const matchesQuery = !query || name.includes(query) || email.includes(query) || dept.toLowerCase().includes(query);
            const matchesDept = selectedDept === 'ALL' || dept === selectedDept;

            return matchesQuery && matchesDept;
        });

        applySorting();
        currentPage = 1;
        renderPage();
        updateBulkBar();
    }

    function applySorting() {
        const sortVal = sortSelect ? sortSelect.value : 'id_desc';

        filteredRows.sort((a, b) => {
            const idA = parseInt(a.getAttribute('data-id') || '0', 10);
            const idB = parseInt(b.getAttribute('data-id') || '0', 10);
            const nameA = (a.getAttribute('data-name') || '').toLowerCase();
            const nameB = (b.getAttribute('data-name') || '').toLowerCase();
            const deptA = (a.getAttribute('data-dept') || '').toLowerCase();
            const deptB = (b.getAttribute('data-dept') || '').toLowerCase();

            switch (sortVal) {
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'name_desc': return nameB.localeCompare(nameA);
                case 'dept_asc': return deptA.localeCompare(deptB);
                case 'id_asc': return idA - idB;
                case 'id_desc':
                default: return idB - idA;
            }
        });
    }

    function renderPage() {
        allRows.forEach(row => row.style.display = 'none');

        const totalItems = filteredRows.length;
        const totalPages = pageSize === 'ALL' ? 1 : Math.ceil(totalItems / pageSize) || 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        const startIdx = pageSize === 'ALL' ? 0 : (currentPage - 1) * pageSize;
        const endIdx = pageSize === 'ALL' ? totalItems : Math.min(startIdx + pageSize, totalItems);

        for (let i = startIdx; i < endIdx; i++) {
            if (filteredRows[i]) {
                filteredRows[i].style.display = '';
            }
        }

        // Update Info
        if (paginationInfo) {
            if (totalItems === 0) {
                paginationInfo.textContent = 'No matching student records found';
            } else {
                paginationInfo.textContent = `Showing ${startIdx + 1}–${endIdx} of ${totalItems} students`;
            }
        }

        // Render Pagination Controls
        if (paginationControls) {
            paginationControls.innerHTML = '';

            const prevBtn = document.createElement('button');
            prevBtn.className = 'page-btn';
            prevBtn.innerHTML = '‹';
            prevBtn.disabled = currentPage <= 1;
            prevBtn.addEventListener('click', () => {
                if (currentPage > 1) {
                    currentPage--;
                    renderPage();
                }
            });
            paginationControls.appendChild(prevBtn);

            for (let p = 1; p <= totalPages; p++) {
                if (totalPages > 7 && Math.abs(p - currentPage) > 2 && p !== 1 && p !== totalPages) {
                    if (p === 2 || p === totalPages - 1) {
                        const ellipsis = document.createElement('span');
                        ellipsis.className = 'page-ellipsis';
                        ellipsis.textContent = '…';
                        ellipsis.style.padding = '0 4px';
                        paginationControls.appendChild(ellipsis);
                    }
                    continue;
                }

                const pageBtn = document.createElement('button');
                pageBtn.className = 'page-btn' + (p === currentPage ? ' active' : '');
                pageBtn.textContent = p;
                pageBtn.addEventListener('click', () => {
                    currentPage = p;
                    renderPage();
                });
                paginationControls.appendChild(pageBtn);
            }

            const nextBtn = document.createElement('button');
            nextBtn.className = 'page-btn';
            nextBtn.innerHTML = '›';
            nextBtn.disabled = currentPage >= totalPages;
            nextBtn.addEventListener('click', () => {
                if (currentPage < totalPages) {
                    currentPage++;
                    renderPage();
                }
            });
            paginationControls.appendChild(nextBtn);
        }
    }

    function updateBulkBar() {
        const checkedBoxes = tbody.querySelectorAll('.student-row-checkbox:checked');
        const count = checkedBoxes.length;

        if (count > 0 && bulkBar && bulkCountEl) {
            bulkBar.classList.add('active');
            bulkCountEl.textContent = `${count} student${count > 1 ? 's' : ''} selected`;
        } else if (bulkBar) {
            bulkBar.classList.remove('active');
        }

        if (selectAllCheckbox) {
            const visibleBoxes = Array.from(tbody.querySelectorAll('tr:not([style*="display: none"]) .student-row-checkbox'));
            if (visibleBoxes.length > 0) {
                selectAllCheckbox.checked = visibleBoxes.every(cb => cb.checked);
                selectAllCheckbox.indeterminate = !selectAllCheckbox.checked && visibleBoxes.some(cb => cb.checked);
            } else {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = false;
            }
        }
    }

    // Event Listeners for Filters
    if (searchInput) {
        searchInput.addEventListener('input', applyFilters);
    }

    if (deptFilter) {
        deptFilter.addEventListener('change', applyFilters);
    }

    if (sortSelect) {
        sortSelect.addEventListener('change', () => {
            applySorting();
            renderPage();
        });
    }

    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', (e) => {
            pageSize = e.target.value === 'ALL' ? 'ALL' : parseInt(e.target.value, 10);
            currentPage = 1;
            renderPage();
        });
    }

    // Checkbox Multi-selection
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', (e) => {
            const isChecked = e.target.checked;
            const visibleRows = filteredRows.filter(row => row.style.display !== 'none');
            visibleRows.forEach(row => {
                const cb = row.querySelector('.student-row-checkbox');
                if (cb) {
                    cb.checked = isChecked;
                    if (isChecked) row.classList.add('selected');
                    else row.classList.remove('selected');
                }
            });
            updateBulkBar();
        });
    }

    tbody.addEventListener('change', (e) => {
        if (e.target.classList.contains('student-row-checkbox')) {
            const row = e.target.closest('tr');
            if (e.target.checked) row.classList.add('selected');
            else row.classList.remove('selected');
            updateBulkBar();
        }
    });

    // Deselect All Button
    const deselectBtn = document.getElementById('bulk-deselect-btn');
    if (deselectBtn) {
        deselectBtn.addEventListener('click', () => {
            tbody.querySelectorAll('.student-row-checkbox').forEach(cb => {
                cb.checked = false;
                cb.closest('tr').classList.remove('selected');
            });
            updateBulkBar();
        });
    }

    // Bulk Delete Action
    const bulkDeleteTrigger = document.getElementById('bulk-delete-trigger-btn');
    if (bulkDeleteTrigger) {
        bulkDeleteTrigger.addEventListener('click', () => {
            const selected = Array.from(tbody.querySelectorAll('.student-row-checkbox:checked')).map(cb => cb.value);
            if (selected.length === 0) return;

            document.getElementById('bulk-delete-modal-count').textContent = selected.length;
            openModal('modal-bulk-delete');
        });
    }

    const confirmBulkDeleteBtn = document.getElementById('confirm-bulk-delete-btn');
    if (confirmBulkDeleteBtn && bulkDeleteForm && bulkDeleteInputContainer) {
        confirmBulkDeleteBtn.addEventListener('click', () => {
            const selected = Array.from(tbody.querySelectorAll('.student-row-checkbox:checked')).map(cb => cb.value);
            bulkDeleteInputContainer.innerHTML = '';
            selected.forEach(id => {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'ids';
                input.value = id;
                bulkDeleteInputContainer.appendChild(input);
            });
            bulkDeleteForm.submit();
        });
    }

    // Initial table render
    applyFilters();
}

/* ==========================================================================
   7. Modals & Sheets Controller
   ========================================================================== */
function initModals() {
    // Backdrop and Close Buttons
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                closeModal(backdrop.id);
            }
        });
    });

    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalId = btn.getAttribute('data-close-modal');
            closeModal(modalId);
        });
    });

    // ESC key closes active modal
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-backdrop.active').forEach(modal => {
                closeModal(modal.id);
            });
        }
    });

    // Trigger buttons
    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const modalId = btn.getAttribute('data-open-modal');
            openModal(modalId);
        });
    });

    // View Details Modal Handler
    document.querySelectorAll('.btn-view-details').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            if (!row) return;

            const id = row.getAttribute('data-id');
            const name = row.getAttribute('data-name');
            const email = row.getAttribute('data-email');
            const dept = row.getAttribute('data-dept');
            const date = row.getAttribute('data-date') || 'Active Enrollment';

            document.getElementById('detail-student-id').textContent = '#' + id;
            document.getElementById('detail-student-name').textContent = name;
            document.getElementById('detail-student-email').textContent = email;
            document.getElementById('detail-student-dept').textContent = dept;
            document.getElementById('detail-student-date').textContent = date;

            const avatar = document.getElementById('detail-student-avatar');
            if (avatar) {
                const initials = name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
                avatar.textContent = initials;
            }

            const editLink = document.getElementById('detail-edit-link');
            if (editLink) {
                editLink.onclick = () => {
                    closeModal('modal-view-details');
                    openEditModalFromData(id, name, email, dept, date);
                };
            }

            openModal('modal-view-details');
        });
    });

    // Single Edit Modal Handler
    document.querySelectorAll('.btn-open-edit-modal').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            if (!row) return;
            const id = row.getAttribute('data-id');
            const name = row.getAttribute('data-name');
            const email = row.getAttribute('data-email');
            const dept = row.getAttribute('data-dept');
            const date = row.getAttribute('data-date');
            openEditModalFromData(id, name, email, dept, date);
        });
    });

    // Single Delete Modal Handler
    document.querySelectorAll('.btn-open-delete-modal').forEach(btn => {
        btn.addEventListener('click', () => {
            const row = btn.closest('tr');
            if (!row) return;
            const id = row.getAttribute('data-id');
            const name = row.getAttribute('data-name');

            document.getElementById('delete-modal-student-name').textContent = name;
            const confirmLink = document.getElementById('confirm-delete-link');
            if (confirmLink) {
                confirmLink.href = `/students/delete/${id}`;
            }
            openModal('modal-confirm-delete');
        });
    });
}

function openEditModalFromData(id, name, email, dept, date) {
    const form = document.getElementById('form-edit-student');
    if (!form) return;

    form.action = `/students/update/${id}`;
    document.getElementById('edit-student-id').value = id;
    document.getElementById('edit-student-name').value = name;
    document.getElementById('edit-student-email').value = email;
    document.getElementById('edit-student-course').value = dept;

    const dateInput = document.getElementById('edit-student-date');
    if (dateInput && date) {
        dateInput.value = date;
    }

    openModal('modal-edit-student');
}

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add('active');
        const firstInput = modal.querySelector('input:not([type="hidden"]), select');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove('active');
    }
}

/* ==========================================================================
   8. Form Validation
   ========================================================================== */
function initFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        const nameInput = form.querySelector('[name="name"]');
        const emailInput = form.querySelector('[name="email"]');
        const courseInput = form.querySelector('[name="course"]');

        const validateName = () => {
            if (!nameInput) return true;
            const valid = nameInput.value.trim().length >= 2;
            setFieldStatus(nameInput, valid, 'Name must be at least 2 characters');
            return valid;
        };

        const validateEmail = () => {
            if (!emailInput) return true;
            const val = emailInput.value.trim();
            const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,63}$/;
            const valid = emailRegex.test(val);
            setFieldStatus(emailInput, valid, 'Please provide a valid email (e.g., student@domain.edu)');
            return valid;
        };

        const validateCourse = () => {
            if (!courseInput) return true;
            const valid = courseInput.value.trim().length > 0;
            setFieldStatus(courseInput, valid, 'Please select or enter a department');
            return valid;
        };

        if (nameInput) nameInput.addEventListener('input', validateName);
        if (emailInput) emailInput.addEventListener('input', validateEmail);
        if (courseInput) courseInput.addEventListener('change', validateCourse);

        form.addEventListener('submit', (e) => {
            const isNameValid = validateName();
            const isEmailValid = validateEmail();
            const isCourseValid = validateCourse();

            if (!isNameValid || !isEmailValid || !isCourseValid) {
                e.preventDefault();
                showToast('error', 'Validation Error', 'Please correct the highlighted form errors.');
            }
        });
    });
}

function setFieldStatus(input, isValid, errorMsg) {
    const group = input.closest('.form-group');
    if (!group) return;

    let errorEl = group.querySelector('.form-error-feedback');
    if (!errorEl) {
        errorEl = document.createElement('div');
        errorEl.className = 'form-error-feedback';
        group.appendChild(errorEl);
    }

    if (isValid) {
        input.style.borderColor = 'var(--accent-green)';
        errorEl.style.display = 'none';
    } else {
        input.style.borderColor = 'var(--accent-red)';
        errorEl.textContent = errorMsg;
        errorEl.style.display = 'block';
    }
}

/* ==========================================================================
   9. Charts (Chart.js with SVG Native Fallback)
   ========================================================================== */
let deptChartInstance = null;
let trendChartInstance = null;

function initCharts() {
    const deptCanvas = document.getElementById('departmentDistributionChart');
    const analyticsCanvas = document.getElementById('analyticsDeptBarChart');

    if (!deptCanvas && !analyticsCanvas) return;

    // Read Data Embedded in DOM
    const rawData = window.DEPARTMENT_DATA || {};
    const labels = Object.keys(rawData);
    const dataValues = Object.values(rawData);

    if (labels.length === 0) {
        labels.push('General');
        dataValues.push(1);
    }

    const appleColors = [
        '#0071e3', '#34c759', '#ff9500', '#af52de', '#30b0c7',
        '#5856d6', '#ff2d55', '#a2845e', '#64d2ff', '#ffd60a'
    ];

    if (typeof Chart !== 'undefined') {
        // Doughnut Chart in Overview
        if (deptCanvas) {
            const ctx = deptCanvas.getContext('2d');
            deptChartInstance = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: dataValues,
                        backgroundColor: appleColors.slice(0, labels.length),
                        borderWidth: 2,
                        borderColor: '#ffffff',
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                font: { family: '-apple-system, sans-serif', size: 11 },
                                boxWidth: 12,
                                padding: 12
                            }
                        }
                    },
                    cutout: '68%'
                }
            });
        }

        // Bar Chart in Analytics
        if (analyticsCanvas) {
            const ctx2 = analyticsCanvas.getContext('2d');
            trendChartInstance = new Chart(ctx2, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Enrolled Students',
                        data: dataValues,
                        backgroundColor: '#0071e3',
                        borderRadius: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1, font: { size: 11 } },
                            grid: { color: 'rgba(0,0,0,0.05)' }
                        },
                        x: {
                            grid: { display: false },
                            ticks: { font: { size: 11 } }
                        }
                    }
                }
            });
        }
    } else {
        // Lightweight SVG Fallback if Chart.js is offline
        renderSvgFallbacks(labels, dataValues, appleColors);
    }
}

function resizeCharts() {
    if (deptChartInstance) deptChartInstance.resize();
    if (trendChartInstance) trendChartInstance.resize();
}

function renderSvgFallbacks(labels, dataValues, colors) {
    const deptWrapper = document.getElementById('chart-wrapper-overview');
    if (deptWrapper) {
        let total = dataValues.reduce((a, b) => a + b, 0) || 1;
        let html = '<div style="width:100%; padding:10px;">';
        labels.forEach((label, i) => {
            const pct = Math.round((dataValues[i] / total) * 100);
            html += `
                <div style="margin-bottom:8px;">
                    <div style="display:flex; justify-content:space-between; font-size:12px; margin-bottom:3px;">
                        <span>${label}</span>
                        <b>${dataValues[i]} (${pct}%)</b>
                    </div>
                    <div style="background:#eee; height:8px; border-radius:4px; overflow:hidden;">
                        <div style="background:${colors[i % colors.length]}; width:${pct}%; height:100%;"></div>
                    </div>
                </div>
            `;
        });
        html += '</div>';
        deptWrapper.innerHTML = html;
    }
}

/* ==========================================================================
   10. Toast Notification Manager
   ========================================================================== */
function initToasts() {
    document.querySelectorAll('.macos-toast').forEach(toast => {
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 300);
        }, 4500);

        const close = toast.querySelector('.toast-close');
        if (close) {
            close.addEventListener('click', () => toast.remove());
        }
    });
}

function showToast(type, title, message) {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'macos-toast';
    toast.innerHTML = `
        <div class="toast-icon ${type}">
            ${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}
        </div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-msg">${message}</div>
        </div>
        <button class="toast-close" type="button">✕</button>
    `;

    toast.querySelector('.toast-close').addEventListener('click', () => toast.remove());
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}
