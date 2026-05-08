import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { studentService } from '../../services/studentService';
import PageHeader from '../../components/common/PageHeader';
import Card from '../../components/common/Card';
import Input, { Select } from '../../components/common/Input';
import Button from '../../components/common/Button';
import toast from 'react-hot-toast';
import { UserCircle, Mail, Phone, GraduationCap, Save } from 'lucide-react';

const ProfilePage = () => {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    grade: '',
    bio: '',
    city: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phone: user.phone || '',
        grade: user.grade || '',
        bio: user.bio || '',
        city: user.city || '',
      });
    }
  }, [user]);

  const validate = () => {
    const errs = {};
    if (!form.firstName.trim()) errs.firstName = 'First name required';
    if (!form.lastName.trim()) errs.lastName = 'Last name required';
    if (!form.email) errs.email = 'Email required';
    return errs;
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      const { data } = await studentService.updateProfile(form);
      updateUser(data);
      toast.success('Profile updated successfully!');
      setEditing(false);
    } catch {
      toast.error('Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  const initials = `${form.firstName?.[0] ?? ''}${form.lastName?.[0] ?? ''}`.toUpperCase();

  return (
    <div className="max-w-3xl mx-auto">
      <PageHeader title="My Profile" subtitle="Manage your personal information and preferences." />

      <div className="grid lg:grid-cols-3 gap-6">
        {/* Avatar card */}
        <Card>
          <div className="text-center">
            <div className="w-24 h-24 bg-indigo-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <span className="text-3xl font-bold text-indigo-600">{initials || <UserCircle size={40} />}</span>
            </div>
            <h3 className="font-semibold text-gray-900 text-lg">{form.firstName} {form.lastName}</h3>
            <p className="text-gray-500 text-sm mt-1">Student</p>
            {form.grade && (
              <p className="text-indigo-600 text-xs font-medium mt-2 bg-indigo-50 px-3 py-1 rounded-full inline-block">
                Grade {form.grade}
              </p>
            )}

            <div className="mt-6 space-y-2 text-sm text-left">
              <div className="flex items-center gap-2 text-gray-600">
                <Mail size={15} className="text-gray-400" /> {form.email || '—'}
              </div>
              {form.phone && (
                <div className="flex items-center gap-2 text-gray-600">
                  <Phone size={15} className="text-gray-400" /> {form.phone}
                </div>
              )}
              {form.city && (
                <div className="flex items-center gap-2 text-gray-600">
                  <GraduationCap size={15} className="text-gray-400" /> {form.city}
                </div>
              )}
            </div>
          </div>
        </Card>

        {/* Edit form */}
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center justify-between mb-6">
              <h3 className="font-semibold text-gray-800">Personal Information</h3>
              {!editing && (
                <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
                  Edit Profile
                </Button>
              )}
            </div>

            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid sm:grid-cols-2 gap-4">
                <Input label="First Name" id="firstName" name="firstName" value={form.firstName} onChange={handleChange} error={errors.firstName} required disabled={!editing} />
                <Input label="Last Name" id="lastName" name="lastName" value={form.lastName} onChange={handleChange} error={errors.lastName} required disabled={!editing} />
              </div>

              <Input label="Email Address" id="email" name="email" type="email" value={form.email} onChange={handleChange} error={errors.email} required disabled />

              <div className="grid sm:grid-cols-2 gap-4">
                <Input label="Phone Number" id="phone" name="phone" type="tel" value={form.phone} onChange={handleChange} disabled={!editing} />
                <Input label="City" id="city" name="city" value={form.city} onChange={handleChange} disabled={!editing} placeholder="Your city" />
              </div>

              <Select label="Current Grade / Year" id="grade" name="grade" value={form.grade} onChange={handleChange} disabled={!editing}>
                <option value="">Select grade</option>
                <option value="9">Grade 9</option>
                <option value="10">Grade 10</option>
                <option value="11">Grade 11</option>
                <option value="12">Grade 12</option>
                <option value="undergrad_1">Undergraduate Year 1</option>
                <option value="undergrad_2">Undergraduate Year 2</option>
                <option value="undergrad_3">Undergraduate Year 3</option>
                <option value="undergrad_4">Undergraduate Year 4</option>
                <option value="graduate">Graduate Student</option>
              </Select>

              <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-gray-700">Bio</label>
                <textarea
                  name="bio"
                  rows={3}
                  value={form.bio}
                  onChange={handleChange}
                  disabled={!editing}
                  placeholder="Tell us a bit about yourself..."
                  className="w-full px-4 py-2.5 rounded-xl border border-gray-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none disabled:bg-gray-50 disabled:text-gray-500"
                />
              </div>

              {editing && (
                <div className="flex gap-3 pt-2">
                  <Button type="submit" loading={loading}>
                    <Save size={16} /> Save Changes
                  </Button>
                  <Button type="button" variant="secondary" onClick={() => setEditing(false)}>
                    Cancel
                  </Button>
                </div>
              )}
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
