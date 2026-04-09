<template>
  <div class="data-import-export">
    <div class="page-header">
      <h2>数据导入导出</h2>
      <p>导入和导出农业数据</p>
    </div>
    
    <el-card class="import-export-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据导入">
          <div class="tab-content">
            <el-upload
              class="upload-demo"
              drag
              action=""
              :on-change="handleFileChange"
              :auto-upload="false"
              multiple
              accept=".xlsx,.xls,.csv"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
              <div class="el-upload__tip" slot="tip">
                支持扩展名：.xlsx, .xls, .csv，单个文件不超过10MB
              </div>
            </el-upload>
            
            <el-button type="primary" @click="importData" :disabled="!files.length">
              导入数据
            </el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="数据导出">
          <div class="tab-content">
            <el-form label-width="120px">
              <el-form-item label="数据类型">
                <el-select v-model="exportDataType">
                  <el-option label="气象数据" value="weather"></el-option>
                  <el-option label="土壤数据" value="soil"></el-option>
                  <el-option label="作物数据" value="crop"></el-option>
                  <el-option label="病虫害数据" value="disease"></el-option>
                  <el-option label="所有数据" value="all"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="yyyy-MM-dd"
                ></el-date-picker>
              </el-form-item>
              
              <el-form-item label="导出格式">
                <el-radio-group v-model="exportFormat">
                  <el-radio label="excel">Excel</el-radio>
                  <el-radio label="csv">CSV</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="exportData">导出数据</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DataImportExport',
  data() {
    return {
      activeTab: '0',
      files: [],
      exportDataType: 'all',
      dateRange: [],
      exportFormat: 'excel'
    }
  },
  methods: {
    handleFileChange(file, fileList) {
      this.files = fileList
    },
    importData() {
      this.$message.success('数据导入成功')
      this.files = []
    },
    exportData() {
      this.$message.success('数据导出开始，请稍候...')
    }
  }
}
</script>

<style scoped>
.data-import-export {
  padding: 10px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
}

.import-export-card {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

.el-form-item {
  margin-bottom: 25px;
}
</style>