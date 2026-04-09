<template>
  <div class="data-collection-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>数据采集任务</span>
      </div>
      <el-form :model="form" label-width="120px">
        <el-form-item label="数据源类型">
          <el-select v-model="form.sourceType" placeholder="请选择数据源类型">
            <el-option label="文件" value="file"></el-option>
            <el-option label="数据库" value="db"></el-option>
            <el-option label="API" value="api"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据源地址" v-if="form.sourceType === 'file'">
          <el-input v-model="form.sourceUrl" placeholder="请输入文件路径或URL"></el-input>
        </el-form-item>
        <el-form-item label="数据库连接" v-if="form.sourceType === 'db'">
          <el-input v-model="form.dbConnectionString" placeholder="请输入数据库连接字符串"></el-input>
        </el-form-item>
        <el-form-item label="API 端点" v-if="form.sourceType === 'api'">
          <el-input v-model="form.apiUrl" placeholder="请输入API端点URL"></el-input>
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="form.taskName" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="startCollection">开始采集</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>采集任务列表</span>
      </div>
      <el-table :data="tasks" style="width: 100%">
        <el-table-column prop="taskName" label="任务名称"></el-table-column>
        <el-table-column prop="sourceType" label="数据源类型"></el-table-column>
        <el-table-column prop="status" label="状态"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewTask(scope.row)">查看</el-button>
            <el-button size="mini" type="danger" @click="deleteTask(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DataCollection',
  data() {
    return {
      form: {
        sourceType: 'file',
        sourceUrl: '',
        dbConnectionString: '',
        apiUrl: '',
        taskName: ''
      },
      tasks: [
        { taskName: '示例任务1', sourceType: '文件', status: '已完成' },
        { taskName: '示例任务2', sourceType: '数据库', status: '采集中' }
      ]
    }
  },
  methods: {
    startCollection() {
      this.$message.success('任务已开始！')
      // 在这里添加开始采集的逻辑
      const newTask = {
        taskName: this.form.taskName,
        sourceType: this.form.sourceType,
        status: '采集中'
      }
      this.tasks.push(newTask)
    },
    viewTask(row) {
      this.$message.info(`查看任务：${row.taskName}`)
    },
    deleteTask(row) {
      this.tasks = this.tasks.filter(task => task !== row)
      this.$message.success('任务已删除！')
    }
  }
}
</script>

<style scoped>
.data-collection-container {
  padding: 10px;
}
.box-card {
  width: 100%;
}
</style>

