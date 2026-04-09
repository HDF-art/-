<template>
  <div class="help-container">
    <el-card class="help-card">
      <div slot="header" class="card-header">
        <span>帮助中心</span>
      </div>
      
      <!-- 系统简介 -->
      <el-card class="help-section">
        <div slot="header" class="section-header">
          <i class="el-icon-info"></i>
          <span>系统简介</span>
        </div>
        <div class="section-content">
          <p>农业大数据联合建模平台是一个集数据管理、模型训练、智能识别于一体的综合性农业大数据平台。</p>
          <p>平台旨在通过大数据分析和人工智能技术，为农业生产提供科学决策支持，提高农业生产效率和质量。</p>
          <p>主要功能包括：数据采集与管理、模型训练与管理、图像识别、数据分析与统计等。</p>
        </div>
      </el-card>
      
      <!-- 功能说明 -->
      <el-card class="help-section">
        <div slot="header" class="section-header">
          <i class="el-icon-menu"></i>
          <span>功能说明</span>
        </div>
        <div class="section-content">
          <h4>1. 数据面板</h4>
          <p>数据面板展示系统的关键指标和数据概览，包括用户数量、农场数量、模型数量等。</p>
          
          <h4>2. 系统管理</h4>
          <p>仅管理员可见，包括用户管理、农场管理和模型管理。</p>
          
          <h4>3. 数据管理</h4>
          <p>仅管理员可见，包括数据采集和数据导入导出功能。</p>
          
          <h4>4. 模型训练</h4>
          <p>仅管理员可见，包括训练任务管理和数据统计分析。</p>
          
          <h4>5. 模型列表</h4>
          <p>展示系统中可用的模型，用户可以查看模型详情和使用说明。</p>
          
          <h4>6. 智能识别</h4>
          <p>包括图像识别和识别记录功能，用户可以上传图像进行识别并查看历史识别记录。</p>
          
          <h4>7. 个人中心</h4>
          <p>包括个人信息和修改密码功能，用户可以管理个人信息和修改登录密码。</p>
        </div>
      </el-card>
      
      <!-- 使用指南 -->
      <el-card class="help-section">
        <div slot="header" class="section-header">
          <i class="el-icon-s-flag"></i>
          <span>使用指南</span>
        </div>
        <div class="section-content">
          <h4>1. 登录系统</h4>
          <p>访问系统登录页面，输入用户名和密码进行登录。请使用您的个人账号登录。</p>
          
          <h4>2. 图像识别</h4>
          <p>在智能识别菜单中选择图像识别，上传需要识别的图像，系统会自动分析并返回识别结果。</p>
          
          <h4>3. 查看识别记录</h4>
          <p>在智能识别菜单中选择识别记录，可以查看历史识别结果和详情。</p>
          
          <h4>4. 管理个人信息</h4>
          <p>在个人中心菜单中选择个人信息，可以查看和修改个人基本信息。</p>
          
          <h4>5. 修改密码</h4>
          <p>在个人中心菜单中选择修改密码，可以修改登录密码。</p>
        </div>
      </el-card>
      
      <!-- 常见问题 -->
      <el-card class="help-section">
        <div slot="header" class="section-header">
          <i class="el-icon-question"></i>
          <span>常见问题</span>
        </div>
        <div class="section-content">
          <el-collapse>
            <el-collapse-item title="如何上传头像？">
              <p>在个人信息页面，点击头像区域或"选择头像"按钮，选择要上传的图像文件，系统会自动上传并更新头像。</p>
            </el-collapse-item>
            <el-collapse-item title="图像识别支持哪些格式？">
              <p>系统支持JPG、PNG格式的图像文件，文件大小不超过2MB。</p>
            </el-collapse-item>
            <el-collapse-item title="如何查看农场信息？">
              <p>在个人信息页面中可以查看农场信息，包括农场名称和农场ID。</p>
            </el-collapse-item>
            <el-collapse-item title="忘记密码怎么办？">
              <p>请联系系统管理员重置密码。</p>
            </el-collapse-item>
            <el-collapse-item title="系统支持哪些浏览器？">
              <p>系统推荐使用Chrome、Firefox、Edge等现代浏览器，以获得最佳体验。</p>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
      
      <!-- 联系支持 -->
      <el-card class="help-section">
        <div slot="header" class="section-header">
          <i class="el-icon-phone"></i>
          <span>联系支持</span>
        </div>
        <div class="section-content">
          <p>如果您在使用过程中遇到任何问题，请通过以下方式联系我们：</p>
          <ul>
            <li>邮箱：support@agri-platform.com</li>
            <li>电话：400-123-4567</li>
            <li>工作时间：周一至周五 9:00-18:00</li>
          </ul>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Help',
  data() {
    return {
    }
  },
  created() {
    console.log('Help component created')
  },
  methods: {
  }
}
</script>

<style scoped>
.help-container {
  padding: 20px 0;
}

.help-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.help-section {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.section-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}

.section-header i {
  margin-right: 8px;
  font-size: 18px;
}

.section-content {
  padding: 15px;
  line-height: 1.6;
  color: #606266;
}

.section-content h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.section-content p {
  margin-bottom: 10px;
}

.section-content ul {
  margin-left: 20px;
  margin-bottom: 10px;
}

.section-content li {
  margin-bottom: 5px;
}

.el-collapse {
  margin-top: 10px;
}

.el-collapse-item__content {
  padding-bottom: 15px;
}
</style>