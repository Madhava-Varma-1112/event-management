require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 5000;
const MONGODB_URI = process.env.MONGODB_URI || process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/eventmanager';

// Connect to MongoDB
mongoose.connect(MONGODB_URI)
  .then(() => {
    console.log('Connected to MongoDB');
    initializeData();
  })
  .catch(err => {
    console.error('MongoDB connection error:', err);
  });

// --- SCHEMAS AND MODELS ---

// Counter Schema for Auto-Incrementing Event IDs
const CounterSchema = new mongoose.Schema({
  _id: { type: String, required: true },
  seq: { type: Number, default: 0 }
});
const Counter = mongoose.model('Counter', CounterSchema);

// User Schema
const UserSchema = new mongoose.Schema({
  username: { type: String, required: true },
  password: { type: String, required: true },
  role: { type: String, required: true }
});
// Composite index on username and role to ensure unique users per role
UserSchema.index({ username: 1, role: 1 }, { unique: true });
const User = mongoose.model('User', UserSchema);

// Event Schema
const EventSchema = new mongoose.Schema({
  id: { type: Number, unique: true },
  title: String,
  date: String,
  time: String,
  category: String,
  description: String,
  organizerName: String,
  facultyCoordinator: String,
  isApproved: { type: Number, default: 0 }, // 0 = Pending, 1 = Approved
  venue: String,
  rewards: String,
  contact: String,
  whatsapp: String,
  creatorUsername: String,
  isDeleteRequested: { type: Number, default: 0 } // 0 = No, 1 = Requested
});

// Auto-increment event id before saving
EventSchema.pre('save', async function(next) {
  if (this.isNew) {
    try {
      const counter = await Counter.findByIdAndUpdate(
        { _id: 'eventId' },
        { $inc: { seq: 1 } },
        { new: true, upsert: true }
      );
      this.id = counter.seq;
    } catch (error) {
      return next(error);
    }
  }
  next();
});
const Event = mongoose.model('Event', EventSchema);

// PendingUpdate Schema
const PendingUpdateSchema = new mongoose.Schema({
  up_event_id: { type: Number, unique: true, required: true },
  new_date: String,
  new_time: String,
  new_venue: String
});
const PendingUpdate = mongoose.model('PendingUpdate', PendingUpdateSchema);

// ReadStatus Schema
const ReadStatusSchema = new mongoose.Schema({
  username: { type: String, required: true },
  event_id: { type: Number, required: true },
  is_read: { type: Number, default: 0 }
});
ReadStatusSchema.index({ username: 1, event_id: 1 }, { unique: true });
const ReadStatus = mongoose.model('ReadStatus', ReadStatusSchema);

// Interest Schema
const InterestSchema = new mongoose.Schema({
  event_id: { type: Number, required: true },
  username: { type: String, required: true }
});
InterestSchema.index({ event_id: 1, username: 1 }, { unique: true });
const Interest = mongoose.model('Interest', InterestSchema);


// --- INITIALIZATION ---
async function initializeData() {
  try {
    // 1. Initialize counter if not exists
    const counterExists = await Counter.findById('eventId');
    if (!counterExists) {
      await new Counter({ _id: 'eventId', seq: 0 }).save();
      console.log('Event counter initialized');
    }

    // 2. Initialize default admin user if not exists
    const adminExists = await User.findOne({ username: 'admin', role: 'admin' });
    if (!adminExists) {
      await new User({ username: 'admin', password: 'admin', role: 'admin' }).save();
      console.log('Default admin user created');
    }
  } catch (error) {
    console.error('Initialization error:', error);
  }
}


// --- REST API ENDPOINTS ---

// 1. Validate Login
app.post('/api/users/login', async (req, res) => {
  const { username, password, role } = req.body;
  try {
    const user = await User.findOne({ username, password, role });
    res.json({ success: !!user });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// 2. Register User
app.post('/api/users/register', async (req, res) => {
  const { username, password, role } = req.body;
  try {
    const exists = await User.findOne({ username, role });
    if (exists) {
      return res.json({ success: false, message: 'User already exists' });
    }
    const newUser = new User({ username, password, role });
    await newUser.save();
    res.json({ success: true });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// 3. User Exists Check
app.post('/api/users/exists', async (req, res) => {
  const { username, role } = req.body;
  try {
    const user = await User.findOne({ username, role });
    res.json({ exists: !!user });
  } catch (error) {
    console.error('User exists check error:', error);
    res.status(500).json({ exists: false, error: error.message });
  }
});

// 4. Update Password
app.post('/api/users/update-password', async (req, res) => {
  const { username, role, newPassword } = req.body;
  try {
    const result = await User.updateOne({ username, role }, { password: newPassword });
    res.json({ success: result.modifiedCount > 0 });
  } catch (error) {
    console.error('Update password error:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

// 5. Add Event
app.post('/api/events', async (req, res) => {
  try {
    const newEvent = new Event(req.body);
    await newEvent.save();
    // After save, the pre-save hook generated the auto-incrementing numeric id
    res.json({ id: newEvent.id });
  } catch (error) {
    console.error('Add event error:', error);
    res.status(500).json({ id: -1, error: error.message });
  }
});

// 6. Get Events By Status
app.get('/api/events/status/:approvalStatus', async (req, res) => {
  const status = Number(req.params.approvalStatus);
  try {
    const events = await Event.find({ isApproved: status });
    res.json(events);
  } catch (error) {
    console.error('Get events by status error:', error);
    res.status(500).json([]);
  }
});

// 7. Approve Event
app.post('/api/events/:id/approve', async (req, res) => {
  const id = Number(req.params.id);
  try {
    await Event.updateOne({ id }, { isApproved: 1 });
    res.json({ success: true });
  } catch (error) {
    console.error('Approve event error:', error);
    res.status(500).json({ success: false });
  }
});

// 8. Delete Event
app.delete('/api/events/:id', async (req, res) => {
  const id = Number(req.params.id);
  try {
    await Event.deleteOne({ id });
    await PendingUpdate.deleteOne({ up_event_id: id });
    await ReadStatus.deleteMany({ event_id: id });
    await Interest.deleteMany({ event_id: id });
    res.json({ success: true });
  } catch (error) {
    console.error('Delete event error:', error);
    res.status(500).json({ success: false });
  }
});

// 9. Request Delete
app.post('/api/events/:id/request-delete', async (req, res) => {
  const id = Number(req.params.id);
  try {
    await Event.updateOne({ id }, { isDeleteRequested: 1 });
    res.json({ success: true });
  } catch (error) {
    console.error('Request delete error:', error);
    res.status(500).json({ success: false });
  }
});

// 10. Cancel Delete Request
app.post('/api/events/:id/cancel-delete-request', async (req, res) => {
  const id = Number(req.params.id);
  try {
    await Event.updateOne({ id }, { isDeleteRequested: 0 });
    res.json({ success: true });
  } catch (error) {
    console.error('Cancel delete request error:', error);
    res.status(500).json({ success: false });
  }
});

// 11. Get Delete Requests
app.get('/api/events/delete-requests', async (req, res) => {
  try {
    const events = await Event.find({ isDeleteRequested: 1 });
    res.json(events);
  } catch (error) {
    console.error('Get delete requests error:', error);
    res.status(500).json([]);
  }
});

// 12. Request Update
app.post('/api/events/:id/request-update', async (req, res) => {
  const id = Number(req.params.id);
  const { date, time, venue } = req.body;
  try {
    await PendingUpdate.updateOne(
      { up_event_id: id },
      { new_date: date, new_time: time, new_venue: venue },
      { upsert: true }
    );
    res.json({ success: true });
  } catch (error) {
    console.error('Request update error:', error);
    res.status(500).json({ success: false });
  }
});

// 13. Get Update Requests (with Event title joined)
app.get('/api/events/update-requests', async (req, res) => {
  try {
    const updates = await PendingUpdate.find();
    const results = [];
    for (const up of updates) {
      const event = await Event.findOne({ id: up.up_event_id });
      results.push({
        up_event_id: up.up_event_id,
        new_date: up.new_date,
        new_time: up.new_time,
        new_venue: up.new_venue,
        title: event ? event.title : 'Unknown Event'
      });
    }
    res.json(results);
  } catch (error) {
    console.error('Get update requests error:', error);
    res.status(500).json([]);
  }
});

// 14. Approve Event Update
app.post('/api/events/:id/approve-update', async (req, res) => {
  const id = Number(req.params.id);
  try {
    const pending = await PendingUpdate.findOne({ up_event_id: id });
    if (pending) {
      await Event.updateOne(
        { id },
        { date: pending.new_date, time: pending.new_time, venue: pending.new_venue }
      );
      await PendingUpdate.deleteOne({ up_event_id: id });
      await ReadStatus.deleteMany({ event_id: id }); // Mark as unread for users since event has changed
    }
    res.json({ success: true });
  } catch (error) {
    console.error('Approve update error:', error);
    res.status(500).json({ success: false });
  }
});

// 15. Reject Event Update
app.post('/api/events/:id/reject-update', async (req, res) => {
  const id = Number(req.params.id);
  try {
    await PendingUpdate.deleteOne({ up_event_id: id });
    res.json({ success: true });
  } catch (error) {
    console.error('Reject update error:', error);
    res.status(500).json({ success: false });
  }
});

// 16. Check if Event is Unread
app.get('/api/events/:id/unread/:username', async (req, res) => {
  const id = Number(req.params.id);
  const username = req.params.username;
  try {
    const status = await ReadStatus.findOne({ event_id: id, username });
    res.json({ unread: !status || status.is_read === 0 });
  } catch (error) {
    console.error('Check unread error:', error);
    res.status(500).json({ unread: true });
  }
});

// 17. Mark Event as Read
app.post('/api/events/:id/mark-read', async (req, res) => {
  const id = Number(req.params.id);
  const { username } = req.body;
  try {
    await ReadStatus.updateOne(
      { event_id: id, username },
      { is_read: 1 },
      { upsert: true }
    );
    res.json({ success: true });
  } catch (error) {
    console.error('Mark read error:', error);
    res.status(500).json({ success: false });
  }
});

// 18. Set Interest
app.post('/api/events/:id/interest', async (req, res) => {
  const id = Number(req.params.id);
  const { username, interested } = req.body;
  try {
    if (interested) {
      await Interest.updateOne(
        { event_id: id, username },
        {},
        { upsert: true }
      );
    } else {
      await Interest.deleteOne({ event_id: id, username });
    }
    res.json({ success: true });
  } catch (error) {
    console.error('Set interest error:', error);
    res.status(500).json({ success: false });
  }
});

// 19. Check if User Interested
app.get('/api/events/:id/interested/:username', async (req, res) => {
  const id = Number(req.params.id);
  const username = req.params.username;
  try {
    const interest = await Interest.findOne({ event_id: id, username });
    res.json({ interested: !!interest });
  } catch (error) {
    console.error('Check interested error:', error);
    res.status(500).json({ interested: false });
  }
});

// 20. Get Interest Count
app.get('/api/events/:id/interest-count', async (req, res) => {
  const id = Number(req.params.id);
  try {
    const count = await Interest.countDocuments({ event_id: id });
    res.json({ count });
  } catch (error) {
    console.error('Get interest count error:', error);
    res.status(500).json({ count: 0 });
  }
});

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
