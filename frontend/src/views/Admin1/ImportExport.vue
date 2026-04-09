<template>
  <div class="import-export-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>数据导入</span>
      </div>
      <el-upload
        class="upload-demo"
        drag
        action="https://jsonplaceholder.typicode.com/posts/"
        multiple>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip" slot="tip">支持上传 .csv, .json, .xlsx 文件</div>
      </el-upload>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>数据导出</span>
      </div>
      <el-table :data="exportData" style="width: 100%">
        <el-table-column prop="name" label="数据集名称"></el-table-column>
        <el-table-column prop="format" label="格式"></el-table-column>
        <el-table-column prop="size" label="大小"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="exportItem(scope.row)">导出</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ImportExport',
  data() {
    return {
      exportData: [
        { name: '农作物病虫害数据集', format: 'csv', size: '10.5 MB' },
        { name: '土壤成分数据', format: 'json', size: '5.2 MB' },
        { name: '气象观测记录', format: 'xlsx', size: '20.1 MB' }
      ]
    }
  },
  methods: {
    exportItem(row) {
      this.$message.success(`开始导出 ${row.name}`)
      // 在这里添加导出逻辑
    }
  }
}
</script>

<style scoped>
.import-export-container {
  padding: 10px;
}
.box-card {
  width: 100%;
}
.upload-demo {
  text-align: center;
}
</style>

