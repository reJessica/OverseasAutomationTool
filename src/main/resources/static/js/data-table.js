$(document).ready(function() {
    let dataTable = null;
    const viewModal = new bootstrap.Modal(document.getElementById('viewModal'));
    let currentTable = null;

    // 从URL获取表名参数
    function getTableNameFromUrl() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('table');
    }

    // 加载可用的数据表列表
    function loadTableList() {
        $.get('/api/tables', function(tables) {
            const tableList = $('#tableList');
            tableList.empty();
            tables.forEach(table => {
                tableList.append(`
                    <li class="nav-item d-flex align-items-center">
                        <a class="nav-link flex-grow-1" href="#" data-table="${table}">
                            <i class="bi bi-table"></i>
                            <span>${table}</span>
                        </a>
                        <button class="delete-table-btn ms-2" data-table="${table}" title="删除表"><i class="bi bi-trash"></i></button>
                    </li>
                `);
            });

            // 检查URL中是否有表名参数
            const tableName = getTableNameFromUrl();
            if (tableName && tables.includes(tableName)) {
                // 只选中.nav-link，避免误触发按钮
                tableList.find('.nav-link[data-table="' + tableName + '"]').addClass('active').trigger('click');
            } else if (tables.length > 0) {
                tableList.find('.nav-link').first().addClass('active').trigger('click');
            }
        });
    }

    // 初始化数据表格
    function initDataTable(tableName) {
        if ($.fn.DataTable.isDataTable('#dataTable')) {
            $('#dataTable').DataTable().clear().destroy();
            $('#dataTable thead').empty();
            $('#dataTable tbody').empty();
        }

        currentTable = tableName;
        $('#currentTableName').text(tableName);

        // 更新URL，但不刷新页面
        const url = new URL(window.location);
        url.searchParams.set('table', tableName);
        window.history.pushState({}, '', url);

        // 首先获取表结构
        $.get(`/api/table/${tableName}/structure`, function(structure) {
            // 动态生成列配置
            const columns = structure.map(column => {
                const columnName = column.COLUMN_NAME;
                const dataType = column.DATA_TYPE;
                
                // 根据数据类型设置不同的渲染方式
                let render = null;
                if (dataType.includes('date') || dataType.includes('time')) {
                    render = function(data) {
                        return data ? new Date(data).toLocaleString('zh-CN') : '';
                    };
                }
                
                return {
                    data: function(row) {
                        // 使用函数来获取数据，避免点号问题
                        return row[columnName];
                    },
                    title: columnName,
                    render: render
                };
            });

            // 添加操作列
            columns.push({
                data: null,
                title: '操作',
                orderable: false,
                render: function(data, type, row) {
                    return `
                        <button class="btn btn-sm btn-primary view-btn" data-id="${row.id}">
                            <i class="bi bi-eye"></i> 查看
                        </button>
                        <button class="btn btn-sm btn-danger delete-btn" data-id="${row.id}">
                            <i class="bi bi-trash"></i> 删除
                        </button>
                    `;
                }
            });

            // 生成表头
            let theadHtml = '<tr>';
            columns.forEach(col => {
                theadHtml += `<th>${col.title}</th>`;
            });
            theadHtml += '</tr>';
            $('#dataTable thead').html(theadHtml);

            // 初始化DataTable
            dataTable = $('#dataTable').DataTable({
                language: {
                    url: '//cdn.datatables.net/plug-ins/1.11.5/i18n/zh.json'
                },
                processing: true,
                serverSide: true,
                ajax: {
                    url: '/api/data',
                    type: 'GET',
                    data: function(d) {
                        // 只传递必要的参数
                        return {
                            tableName: tableName,
                            start: d.start,
                            length: d.length,
                            draw: d.draw,
                            order: d.order ? d.order[0] : null,
                            search: d.search ? d.search.value : ''
                        };
                    },
                    dataSrc: function(json) {
                        return json.data || [];
                    }
                },
                columns: columns,
                order: [[0, 'desc']],
                pageLength: 10,
                responsive: true,
                dom: 'frtip',
                buttons: [
                    {
                        extend: 'excel',
                        text: '<i class="bi bi-file-earmark-excel"></i> Excel',
                        className: 'btn btn-success btn-sm d-none', // 隐藏
                        exportOptions: {
                            columns: ':not(:last-child)'
                        }
                    },
                    {
                        extend: 'pdf',
                        text: '<i class="bi bi-file-earmark-pdf"></i> PDF',
                        className: 'btn btn-danger btn-sm d-none', // 隐藏
                        exportOptions: {
                            columns: ':not(:last-child)'
                        }
                    },
                    {
                        extend: 'print',
                        text: '<i class="bi bi-printer"></i> 打印',
                        className: 'btn btn-info btn-sm d-none', // 隐藏
                        exportOptions: {
                            columns: ':not(:last-child)'
                        }
                    }
                ]
            });
        });
    }

    // 表选择事件
    $('#tableList').on('click', '.nav-item', function(e) {
        // 如果点击的是删除按钮，不切换表
        if ($(e.target).closest('.delete-table-btn').length > 0) {
            return;
        }
        e.preventDefault();
        const tableName = $(this).find('.nav-link').data('table');
        if (!tableName) return;
        // 更新活动状态
        $('#tableList .nav-link').removeClass('active');
        $(this).find('.nav-link').addClass('active');
        initDataTable(tableName);
    });

    // 刷新按钮事件
    $('#refreshBtn').on('click', function() {
        if (currentTable) {
            dataTable.ajax.reload();
        }
    });

    // 导出按钮事件
    $('#exportBtn').on('click', function() {
        if (dataTable) {
            dataTable.button('.buttons-excel').trigger();
        }
    });

    // 查看按钮点击事件
    $('#dataTable').on('click', '.view-btn', function() {
        const id = $(this).data('id');
        
        $.get(`/api/data/${currentTable}/${id}`, function(data) {
            let content = '<div class="table-responsive"><table class="table table-bordered">';
            for (let key in data) {
                if (key !== 'id') {
                    content += `
                        <tr>
                            <th style="width: 150px;">${key}</th>
                            <td>${data[key]}</td>
                        </tr>
                    `;
                }
            }
            content += '</table></div>';
            
            $('#detailContent').html(content);
            viewModal.show();
        });
    });

    // 删除按钮点击事件
    $('#dataTable').on('click', '.delete-btn', function() {
        const id = $(this).data('id');
        
        if (confirm('确定要删除这条记录吗？')) {
            $.ajax({
                url: `/api/data/${currentTable}/${id}`,
                method: 'DELETE',
                success: function(response) {
                    dataTable.ajax.reload();
                    alert('删除成功！');
                },
                error: function(xhr) {
                    alert('删除失败：' + xhr.responseText);
                }
            });
        }
    });

    // 删除表按钮事件
    $('#tableList').on('click', '.delete-table-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();
        const tableName = $(this).data('table');
        if (confirm(`确定要删除表【${tableName}】吗？此操作不可恢复！`)) {
            $.ajax({
                url: `/api/table/${tableName}`,
                type: 'DELETE',
                success: function() {
                    alert('表已删除！');
                    loadTableList();
                },
                error: function(xhr) {
                    alert('删除失败：' + (xhr.responseText || '未知错误'));
                }
            });
        }
    });

    // 初始加载表列表
    loadTableList();
}); 